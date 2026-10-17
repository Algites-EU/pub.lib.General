from eu.algites.lib.common.object.rendering.ais_rendering_output_utils import AIsRenderingOutputUtils

class AIxRuntimeException(RuntimeError):
    """Runtime error with supplier evaluated in the requested rendering context."""
    def __init__(self, aMessageSupplier, aCause=None, aMessageOutputPurpose=None,
                 aRenderingOutputFormat=None, aLocale=None):
        supplier = aMessageSupplier if callable(aMessageSupplier) else lambda: str(aMessageSupplier)
        text=AIsRenderingOutputUtils.resolveStringOutput(supplier,aMessageOutputPurpose,aRenderingOutputFormat,aLocale)
        super().__init__(text)
        self._message_output_purpose=aMessageOutputPurpose
        if aCause is not None: self.__cause__=aCause
    def getMessageOutputPurpose(self): return self._message_output_purpose
