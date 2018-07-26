package krzysg;

/**
 * Originally created by Tobias
 */

import bdv.spimdata.SequenceDescriptionMinimal;
import bdv.spimdata.SpimDataMinimal;
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
import java.util.HashMap;

public class AprViewer {
	public static void main(final String[] args) {
		final String fn = "/Users/krzysg/zebra7GB.h5";

		final int[] cellDimensions = new int[] { 32, 32, 32 };
		final int numLevels = 3;

		// ======================== Create APR =========================
		final JavaAPR apr = new JavaAPR();

		// ======================== Load APR ===========================
		System.out.println( "Loading [" + fn + "]" );
		apr.read( fn );
		System.out.println( "Img Size (w/h/d): " + apr.width() + "/" + apr.height() + "/" + apr.depth() );

		final int setupId = 0;
		final int timepointId = 0;

		final File basePath = new File( fn ).getParentFile();

		final HashMap< Integer, TimePoint > timepointMap = new HashMap<>();
		timepointMap.put( timepointId, new TimePoint( timepointId ) );
		final HashMap< Integer, BasicViewSetup > setupMap = new HashMap<>();
		setupMap.put( setupId, new BasicViewSetup( setupId, "apr", null, null ) );
		final SequenceDescriptionMinimal seq = new SequenceDescriptionMinimal( new TimePoints( timepointMap ), setupMap, null, null );

		final HashMap< ViewId, ViewRegistration > registrations = new HashMap<>();
		final AffineTransform3D calib = new AffineTransform3D();
		calib.set(
				1, 0, 0, 0,
				0, 1, 0, 0,
				0, 0, 4, 0 );
		registrations.put( new ViewId( timepointId, setupId ), new ViewRegistration( timepointId, setupId, calib ) );

		final SpimDataMinimal spimData = new SpimDataMinimal( basePath, seq, new ViewRegistrations( registrations ) );
		final APRImgLoader imgLoader = new APRImgLoader( apr, cellDimensions, numLevels);
		seq.setImgLoader( imgLoader );

		BdvFunctions.show( spimData );
	}
}
