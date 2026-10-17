from eu.algites.lib.common.exception.aix_runtime_exception import AIxRuntimeException
class AIxDevelopmentErrorException(AIxRuntimeException):
    def __init__(self, aMessage, aCause=None, aMessageOutputPurpose=None, aRenderingOutputFormat=None, aLocale=None):
        supplier=aMessage if callable(aMessage) else lambda: str(aMessage)
        super().__init__(lambda: r'\Development error:' + supplier(),aCause,aMessageOutputPurpose,aRenderingOutputFormat,aLocale)
