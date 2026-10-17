from __future__ import annotations
from dataclasses import fields, is_dataclass
from functools import lru_cache
from eu.algites.lib.common.object.props.labels.aia_field_label import AIaFieldLabel
from eu.algites.lib.common.object.rendering.ais_rendering_output_utils import AIsRenderingOutputUtils as State
from eu.algites.lib.common.object.rendering.ain_rendering_output_builtin_format import AInRenderingOutputBuiltinFormat as Format
from eu.algites.lib.common.object.rendering.ain_rendering_output_builtin_purpose import AInRenderingOutputBuiltinPurpose as Purpose

class AIsFieldLabelUtils:
    @staticmethod
    def _property_name(name: str) -> str:
        if name.startswith('get') and len(name)>3: return name[3].lower()+name[4:]
        if name.startswith('is') and len(name)>2: return name[2].lower()+name[3:]
        return name

    @staticmethod
    def _annotation(cls: type, name: str):
        # Superclasses searched from most specific to least specific.
        for owner in cls.__mro__:
            if owner is object: continue
            # Interface/protocol methods precede overriding class annotations.
            meta = None
            for attr_name, item in owner.__dict__.items():
                if AIsFieldLabelUtils._property_name(attr_name)==name:
                    fn = item.fget if isinstance(item,property) else item
                    meta = getattr(fn, '__field_label__', meta)
            meta = owner.__dict__.get('__field_labels__', {}).get(name, meta)
            # Class fields and dataclass metadata override getter annotations.
            if is_dataclass(owner):
                for f in fields(owner):
                    if f.name==name: meta=f.metadata.get('AIaFieldLabel',meta)
            if meta is not None: return meta
        return None

    @staticmethod
    def _code(enum_value, custom: str):
        if enum_value is not None and getattr(enum_value,'name','DEFAULT') != 'DEFAULT': return enum_value.code()
        return custom or None

    @staticmethod
    def _resolver(cls):
        if cls is None: return None
        try: return cls()
        except (TypeError,ValueError): return None

    @staticmethod
    def findLabel(aOwnerClass: type, aPropertyName: str,
                  aRenderingOutputPurpose=None,aRenderingOutputFormat=None,aRenderingOutputLocale=None,
                  aRoleCode: str = AIaFieldLabel.DEFAULT_ROLE_CODE) -> str | None:
        ann=AIsFieldLabelUtils._annotation(aOwnerClass,aPropertyName)
        is_default_role = aRoleCode == AIaFieldLabel.DEFAULT_ROLE_CODE
        if ann is None: return aPropertyName if is_default_role else None
        if ann.labels and ann.labelResolver: raise ValueError('Choose only one of labels/labelResolver')
        purpose=aRenderingOutputPurpose or (r.resolvePurpose() if (r:=AIsFieldLabelUtils._resolver(ann.purposeResolver)) else None) or State.usedRenderingOutputPurpose()
        fmt=aRenderingOutputFormat or (r.resolveFormat() if (r:=AIsFieldLabelUtils._resolver(ann.formatResolver)) else None) or State.usedRenderingOutputFormat()
        locale=aRenderingOutputLocale or (r.resolveLocale() if (r:=AIsFieldLabelUtils._resolver(ann.localeResolver)) else None) or State.usedRenderingOutputLocale()
        resolver=AIsFieldLabelUtils._resolver(ann.labelResolver)
        if resolver is not None:
            if hasattr(resolver, 'resolveLabelForRole'):
                result=resolver.resolveLabelForRole(aOwnerClass,aPropertyName,aRoleCode,purpose,fmt,locale)
            else:
                result=resolver.resolveLabel(aOwnerClass,aPropertyName,purpose,fmt,locale) if is_default_role else None
            if result and result.strip(): return result
        pcode=purpose.code() if purpose else None
        fcode=fmt.code() if fmt else None
        tags=[str(locale), str(locale).replace('-','_'), str(locale).replace('_','-'), str(locale).replace('_','-').split('-')[0]] if locale else []
        mappings={}
        for entry in ann.labels:
            ec=AIsFieldLabelUtils._code(entry.purpose,entry.purposeCode)
            fc=AIsFieldLabelUtils._code(entry.format,entry.formatCode)
            if not ec: ec = Purpose.DEFAULT.code()
            mappings[(entry.roleCode,ec,fc,entry.localeCode or None)]=entry.label
        def matching(code):
            for key in ([(aRoleCode,code,fcode,tag) for tag in tags] + [(aRoleCode,code,fcode,None)]
                        + [(aRoleCode,code,None,tag) for tag in tags] + [(aRoleCode,code,None,None)]):
                label=mappings.get(key)
                if label and label.strip(): return label
            return None
        result = matching(pcode) or (matching(Purpose.DEFAULT.code()) if pcode != Purpose.DEFAULT.code() else None)
        if result: return result
        if ann.label and ann.roleCode == aRoleCode: return ann.label
        return aPropertyName if is_default_role else None

    @staticmethod
    def isPropertyPresent(aOwnerClass: type, aPropertyName: str) -> bool:
        if aOwnerClass is None or not aPropertyName: return False
        for cls in aOwnerClass.__mro__:
            if aPropertyName in getattr(cls,'__annotations__',{}) or aPropertyName in vars(cls): return True
            for name, fn in vars(cls).items():
                if callable(fn) and AIsFieldLabelUtils._property_name(name)==aPropertyName: return True
        return False

    @staticmethod
    def requirePropertyExists(aOwnerClass: type, aPropertyName: str) -> None:
        if not AIsFieldLabelUtils.isPropertyPresent(aOwnerClass,aPropertyName):
            raise ValueError(f'Unknown property {aPropertyName!r} for {aOwnerClass}')
