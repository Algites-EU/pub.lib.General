from typing import Protocol
from eu.algites.lib.common.object.rendering.aii_rendering_output_format import AIiRenderingOutputFormat
class AIiRenderingOutputFormatResolver(Protocol):
    def resolveFormat(self) -> AIiRenderingOutputFormat: ...
