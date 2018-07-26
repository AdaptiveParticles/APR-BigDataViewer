package krzysg;

/**
 * APR Viewer
 * Copyright (C) 2018
 * Krzysztof Gonciarz, Tobias Pietzsch
 */

import bdv.spimdata.SequenceDescriptionMinimal;
import bdv.spimdata.SpimDataMinimal;
import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import mosaic.JavaAPR;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.registration.ViewRegistration;
import mpicbg.spim.data.registration.ViewRegistrations;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.TimePoints;
import mpicbg.spim.data.sequence.ViewId;
import net.imglib2.realtransform.AffineTransform3D;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;


public class AprViewer {
	public static void main(final String[] args) {
	    // ------------ Verify input arguments -------------------
		if (args.length != 1) {
			System.err.println("Please provide exactly one APR file as a argument. Instead you provided: " + Arrays.asList(args));
			return;
		}

		final String inputFileName = args[0];
		final File inputFile = new File(inputFileName);
		if (!inputFile.exists() || inputFile.isDirectory()) {
            System.err.println("Input file must be a regular file instead you provided: " + inputFile);
            return;
        }

        // ------------ Load APR ---------------------------------
		final JavaAPR apr = new JavaAPR();
		System.out.println( "Loading [" + inputFileName + "]" );
		apr.read( inputFileName );
		System.out.println( "Loaded image size (w/h/d): " + apr.width() + "/" + apr.height() + "/" + apr.depth() );

        // ------------ Set BDV stuff ----------------------------
		final File basePath = inputFile.getParentFile();

		final HashMap< Integer, TimePoint > timepointMap = new HashMap<>();
		final int timepointId = 0;
		timepointMap.put( timepointId, new TimePoint( timepointId ) );
		final HashMap< Integer, BasicViewSetup > setupMap = new HashMap<>();
		final int setupId = 0;
		setupMap.put( setupId, new BasicViewSetup( setupId, "APR", null, null ) );
		final int[] cellDimensions = new int[] { 32, 32, 32 };
		final int numLevels = 3;
		final APRImgLoader imgLoader = new APRImgLoader( apr, cellDimensions, numLevels);
		final SequenceDescriptionMinimal seq = new SequenceDescriptionMinimal( new TimePoints( timepointMap ), setupMap, imgLoader, null );

		final HashMap< ViewId, ViewRegistration > registrations = new HashMap<>();
		final AffineTransform3D calibration = new AffineTransform3D();
		calibration.set(
				1, 0, 0, 0,
				0, 1, 0, 0,
				0, 0, 1, 0 );
		registrations.put( new ViewId( timepointId, setupId ), new ViewRegistration(timepointId, setupId, calibration) );

		final SpimDataMinimal spimData = new SpimDataMinimal( basePath, seq, new ViewRegistrations(registrations) );

        // ------------ Run BDV ----------------------------------
        BdvFunctions.show(spimData, Bdv.options().frameTitle("APR viewer [" + inputFile.getName() + "]"));
    }
}
