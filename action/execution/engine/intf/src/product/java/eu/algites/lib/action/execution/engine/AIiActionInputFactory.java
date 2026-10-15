package eu.algites.lib.action.execution.engine;

/**
 * Pure factory resolving a scheduled input from immutable predecessor results.
 * The factory is registered before plan freeze and must not cause external effects.
 * @param <IN> produced input type
 */
public interface AIiActionInputFactory<IN extends AIiActionInput> {

    /** Creates an input once dependencies have resolved; may fail before an action attempt. */
    IN createInput(AIiActionInputResolutionContext aContext) throws Exception;
}
