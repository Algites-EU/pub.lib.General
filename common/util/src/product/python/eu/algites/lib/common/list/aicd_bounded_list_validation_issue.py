from __future__ import annotations
from dataclasses import dataclass
from eu.algites.lib.common.validation.aii_validation_issue import AIiValidationIssue

@dataclass(frozen=True, slots=True)
class AIcdBoundedListValidationIssue(AIiValidationIssue):
    code: str
    actualSize: int
    bound: int

    def getCode(self) -> str: return self.code
    def getPath(self) -> str: return ''
    def getRelatedTopicText(self, aTopicId: str, aLocale: str | None) -> str | None: return None
    def getMessage(self, aLocale: str | None) -> str:
        if aLocale is None:
            return f'{self.code}(actualSize={self.actualSize}, bound={self.bound})'
        cs = str(aLocale).replace('_', '-').split('-', 1)[0].lower() == 'cs' 
        if self.code == 'MIN_ITEMS':
            return (f'Seznam musí obsahovat alespoň {self.bound} položek; aktuálně obsahuje {self.actualSize}.' if cs else
                    f'List must contain at least {self.bound} items; current size is {self.actualSize}.')
        if self.code == 'MAX_ITEMS':
            return (f'Seznam smí obsahovat nejvýše {self.bound} položek; aktuálně obsahuje {self.actualSize}.' if cs else
                    f'List must contain at most {self.bound} items; current size is {self.actualSize}.')
        raise ValueError(f'Unknown cardinality issue code: {self.code}')
