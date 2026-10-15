package eu.algites.lib.action.execution.engine;

/**
 * Explicit per-attempt worker context with typed provider interaction.
 * The context is execution-scoped, NOT an implicit mutable transaction controller.
 * @param <I> provider-facing interaction view specialized by the application
 */
public interface AIiActionExecutionContext<I extends eu.algites.lib.action.interaction.AIiActionInteractionProviderToCaller<?, ?, ?, ?, ?>>
        extends eu.algites.lib.action.execution.common.AIiExecutionContext {

    /** Immutable stable identity of the scheduled action. */
    String getActionId();

    /** Provider-only interaction direction. */
    I getInteraction();

    /** Cooperative request status, interpreted by the transaction coordinator. */
    boolean isCancellationRequested();
}
