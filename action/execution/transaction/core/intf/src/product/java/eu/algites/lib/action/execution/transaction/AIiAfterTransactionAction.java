package eu.algites.lib.action.execution.transaction;

/**
 * Predeclared final-result observer invoked after definitive commit or rollback.
 * Neither method may alter the original transaction or change its final outcome.
 * The callback can start a separate, new top-level execution plan.
 * @param <TSR> caller-specific successful transaction result shape
 * @param <TFR> caller-specific failure result shape
 * @param <CTX> caller-specific independent action context
 */
public interface AIiAfterTransactionAction<TSR extends AIiTransactionSuccessResult<?>,
        TFR extends AIiTransactionFailureResult<?>,
        CTX extends AIiAfterTransactionContext> {

    /** Reacts to an irrevocably committed transaction. */
    void afterSuccess(TSR aResult, CTX aContext) throws Exception;

    /** Reacts after rollback and full failure status have been determined. */
    void afterFailure(TFR aResult, CTX aContext) throws Exception;
}
