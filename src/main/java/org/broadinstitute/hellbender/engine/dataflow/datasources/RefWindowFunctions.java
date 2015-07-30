package org.broadinstitute.hellbender.engine.dataflow.datasources;

import com.google.cloud.dataflow.sdk.transforms.SerializableFunction;
import org.broadinstitute.hellbender.utils.SimpleInterval;
import org.broadinstitute.hellbender.utils.read.GATKRead;
import org.broadinstitute.hellbender.engine.dataflow.transforms.composite.AddContextDataToRead;

/**
 * A library of reference window functions suitable for passing in to transforms such as {@link AddContextDataToRead}.
 * These are functions from {@link GATKRead} to {@link SimpleInterval}, with the output interval representing
 * the bases of reference context that should be retrieved for the input read.
 */
public class RefWindowFunctions {

    /**
     * A function for requesting only reference bases that directly overlap each read
     */
    public static final SerializableFunction<GATKRead, SimpleInterval> IDENTITY_FUNCTION = read -> new SimpleInterval(read);

    /**
     * A function for requesting a fixed number of extra bases of reference context on either side
     * of each read. For example, a "new FixedWindowFunction(3, 5)" would request 3 extra reference bases
     * before each read and 5 extra bases after each read, in addition to the reference bases spanning
     * each read.
     */
    public static final class FixedWindowFunction implements SerializableFunction<GATKRead, SimpleInterval> {
        private static final long serialVersionUID = 1L;

        private final int leadingWindowBases;
        private final int trailingWindowBases;

        /**
         * @param leadingWindowBases number of bases of additional reference context to request before each read's start position
         * @param trailingWindowBases number of bases of additional reference context to request after each read's end position
         */
        public FixedWindowFunction( final int leadingWindowBases, final int trailingWindowBases ) {
            this.leadingWindowBases = leadingWindowBases;
            this.trailingWindowBases = trailingWindowBases;
        }

        @Override
        public SimpleInterval apply( GATKRead read ) {
            // TODO: truncate interval at contig end (requires a sequence dictionary)
            return new SimpleInterval(read.getContig(), Math.max(read.getStart() - leadingWindowBases, 1), read.getEnd() + trailingWindowBases);
        }
    }
}
