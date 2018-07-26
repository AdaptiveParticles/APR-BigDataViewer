package krzysg;

import bdv.AbstractViewerSetupImgLoader;
import bdv.ViewerImgLoader;
import bdv.ViewerSetupImgLoader;
import bdv.cache.CacheControl;
import bdv.img.cache.CacheArrayLoader;
import bdv.img.cache.VolatileGlobalCellCache;
import bdv.img.hdf5.MipmapInfo;
import bdv.util.MipmapTransforms;
import mpicbg.spim.data.generic.sequence.ImgLoaderHint;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.volatiles.CacheHints;
import net.imglib2.cache.volatiles.LoadingStrategy;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileShortArray;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.cell.CellImg;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.volatiles.VolatileUnsignedShortType;
import net.imglib2.util.Intervals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import mosaic.JavaAPR;

public class APRImgLoader implements ViewerImgLoader {
    private final VolatileGlobalCellCache cache;
    private final APRSetupImgLoader setupImgLoader;

    public APRImgLoader(final JavaAPR apr, final int[] cellDimensions, final int numLevels ) {
        cache = new VolatileGlobalCellCache(numLevels, 8);

        final double[][] resolutions = new double[ numLevels ][];
        final int[][] subdivisions = new int[ numLevels ][];
        final AffineTransform3D[] transforms = new AffineTransform3D[ numLevels ];
        for (int level = 0; level < numLevels; ++level) {
            final double s = 1 << level;
            resolutions[ level ] = new double[] { s, s, s };
            subdivisions[ level ] = cellDimensions;
            transforms[ level ] = MipmapTransforms.getMipmapTransformDefault( resolutions[ level ] );
        }
        final MipmapInfo mipmapInfo = new MipmapInfo( resolutions, transforms, subdivisions );

        final long[][] dimensions = new long[ numLevels ][];
        dimensions[ 0 ] = new long[] { apr.width(), apr.height(), apr.depth() };
        for ( int level = 1; level < numLevels; ++level )
            dimensions[ level ] = new long[] {
                    dimensions[ level - 1 ][ 0 ] / 2,
                    dimensions[ level - 1 ][ 1 ] / 2,
                    dimensions[ level - 1 ][ 2 ] / 2 };

        final int setupId = 0;

        final APRArrayLoader loader = new APRArrayLoader( apr );
        setupImgLoader = new APRSetupImgLoader( setupId, dimensions, mipmapInfo, cache, loader );
    }

    @Override
    public ViewerSetupImgLoader< ?, ? > getSetupImgLoader(final int setupId) {
        return setupImgLoader;
    }

    @Override
    public CacheControl getCacheControl() {
        return cache;
    }

    static class APRArrayLoader implements CacheArrayLoader< VolatileShortArray > {
        private final JavaAPR apr;
        private final ThreadLocal<ShortBuffer> tlBuffer = ThreadLocal.withInitial( () -> ByteBuffer.allocateDirect( 2 ).order( ByteOrder.nativeOrder() ).asShortBuffer() );

        public APRArrayLoader( final JavaAPR apr )
        {
            this.apr = apr;
        }

        @Override
        public VolatileShortArray loadArray(final int timepoint, final int setup, final int level, final int[] dimensions, final long[] min ) throws InterruptedException {
            final int size = ( int ) Intervals.numElements( dimensions );
            if ( tlBuffer.get().capacity() < size )
                tlBuffer.set( ByteBuffer.allocateDirect( 2 * size ).order( ByteOrder.nativeOrder() ).asShortBuffer() );
            final ShortBuffer buffer = tlBuffer.get();
            buffer.rewind();
            apr.reconstructToBuffer(
                    ( int ) min[ 0 ],
                    ( int ) min[ 1 ],
                    ( int ) min[ 2 ],
                    dimensions[ 0 ],
                    dimensions[ 1 ],
                    dimensions[ 2 ],
                    level,
                    buffer );

            final short[] array = new short[ ( int ) Intervals.numElements( dimensions ) ];
            buffer.get( array, 0, size );
            return new VolatileShortArray( array, true );
        }

        @Override
        public int getBytesPerElement()
        {
            return 2;
        }
    }

    static class APRSetupImgLoader extends AbstractViewerSetupImgLoader< UnsignedShortType, VolatileUnsignedShortType > {
        private final int setupId;

        private final long[][] dimensions;

        /**
         * Description of available mipmap levels for the setup. Contains for
         * each mipmap level, the subsampling factors and subdivision block
         * sizes.
         */
        private final MipmapInfo mipmapInfo;

        private final VolatileGlobalCellCache cache;

        private final CacheArrayLoader< VolatileShortArray > loader;

        protected APRSetupImgLoader(
                final int setupId,
                final long[][] dimensions,
                final MipmapInfo mipmapInfo,
                final VolatileGlobalCellCache cache,
                final CacheArrayLoader< VolatileShortArray > loader ) {
            super( new UnsignedShortType(), new VolatileUnsignedShortType() );
            this.setupId = setupId;
            this.dimensions = dimensions;
            this.mipmapInfo = mipmapInfo;
            this.cache = cache;
            this.loader = loader;
        }

        @Override
        public RandomAccessibleInterval< UnsignedShortType > getImage(final int timepointId, final int level, final ImgLoaderHint... hints ) {
            return prepareCachedImage( timepointId, level, LoadingStrategy.BLOCKING, type );
        }

        @Override
        public RandomAccessibleInterval< VolatileUnsignedShortType > getVolatileImage( final int timepointId, final int level, final ImgLoaderHint... hints ) {
            return prepareCachedImage( timepointId, level, LoadingStrategy.BUDGETED, volatileType );
        }

        /**
         * (Almost) create a {@link CellImg} backed by the cache.
         * The type should be either {@link UnsignedShortType} and {@link VolatileUnsignedShortType}.
         */
        protected < T extends NativeType< T >> RandomAccessibleInterval< T > prepareCachedImage(final int timepointId, final int level, final LoadingStrategy loadingStrategy, final T type ) {
            final CellGrid grid = new CellGrid( dimensions[ level ], mipmapInfo.getSubdivisions()[ level ] );
            final int priority = mipmapInfo.getMaxLevel() - level;
            final CacheHints cacheHints = new CacheHints( loadingStrategy, priority, false );
            return cache.createImg( grid, timepointId, setupId, level, cacheHints, loader, type );
        }

        @Override
        public double[][] getMipmapResolutions()
        {
            return mipmapInfo.getResolutions();
        }

        @Override
        public AffineTransform3D[] getMipmapTransforms()
        {
            return mipmapInfo.getTransforms();
        }

        @Override
        public int numMipmapLevels()
        {
            return mipmapInfo.getNumLevels();
        }
    }
}
