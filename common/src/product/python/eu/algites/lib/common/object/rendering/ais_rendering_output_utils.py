from __future__ import annotations
from threading import local
import locale as _locale
from eu.algites.lib.common.object.rendering.ain_rendering_output_builtin_format import AInRenderingOutputBuiltinFormat
from eu.algites.lib.common.object.rendering.ain_rendering_output_builtin_purpose import AInRenderingOutputBuiltinPurpose

class AIsRenderingOutputUtils:
    _state = local()
    @staticmethod
    def usedRenderingOutputPurpose():
        return getattr(AIsRenderingOutputUtils._state,'purpose',AInRenderingOutputBuiltinPurpose.DEFAULT)
    @staticmethod
    def useRenderingOutputPurpose(aRenderingOutputPurpose):
        AIsRenderingOutputUtils._state.purpose = aRenderingOutputPurpose
    @staticmethod
    def isUsedRenderingOutputPurpose(aRenderingOutputPurpose) -> bool:
        # Preserve Java semantics (identity inequality).
        return AIsRenderingOutputUtils.usedRenderingOutputPurpose() is not aRenderingOutputPurpose
    @staticmethod
    def usedRenderingOutputFormat():
        return getattr(AIsRenderingOutputUtils._state,'format',AInRenderingOutputBuiltinFormat.DEFAULT)
    @staticmethod
    def useRenderingOutputFormat(aRenderingOutputFormat):
        AIsRenderingOutputUtils._state.format = aRenderingOutputFormat
    @staticmethod
    def isUsedRenderingOutputFormat(aRenderingOutputFormat) -> bool:
        return AIsRenderingOutputUtils.usedRenderingOutputFormat() is not aRenderingOutputFormat
    @staticmethod
    def usedRenderingOutputLocale():
        return getattr(AIsRenderingOutputUtils._state,'locale',_locale.getlocale()[0] or 'en')
    @staticmethod
    def useRenderingOutputLocale(aRenderingOutputLocale):
        AIsRenderingOutputUtils._state.locale = aRenderingOutputLocale
    @staticmethod
    def isUsedRenderingOutputLocale(aRenderingOutputLocale) -> bool:
        return AIsRenderingOutputUtils.usedRenderingOutputLocale() == aRenderingOutputLocale
    @staticmethod
    def resolveStringOutput(aStringOutputSupplier, aRenderingOutputPurpose=None,
                            aRenderingOutputFormat=None, aRenderingLocale=None) -> str:
        if aStringOutputSupplier is None: raise ValueError('Supplier must not be null')
        cls=AIsRenderingOutputUtils
        prev=(cls.usedRenderingOutputPurpose(),cls.usedRenderingOutputFormat(),cls.usedRenderingOutputLocale())
        try:
            cls.useRenderingOutputPurpose(aRenderingOutputPurpose if aRenderingOutputPurpose is not None else prev[0])
            cls.useRenderingOutputFormat(aRenderingOutputFormat if aRenderingOutputFormat is not None else prev[1])
            cls.useRenderingOutputLocale(aRenderingLocale if aRenderingLocale is not None else prev[2])
            return aStringOutputSupplier()
        finally:
            cls.useRenderingOutputPurpose(prev[0]);cls.useRenderingOutputFormat(prev[1]);cls.useRenderingOutputLocale(prev[2])
