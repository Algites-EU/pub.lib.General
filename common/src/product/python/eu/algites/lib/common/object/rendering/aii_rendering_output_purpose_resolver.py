from typing import Protocol
from eu.algites.lib.common.object.rendering.aii_rendering_output_purpose import AIiRenderingOutputPurpose
class AIiRenderingOutputPurposeResolver(Protocol):
    def resolvePurpose(self) -> AIiRenderingOutputPurpose: ...
