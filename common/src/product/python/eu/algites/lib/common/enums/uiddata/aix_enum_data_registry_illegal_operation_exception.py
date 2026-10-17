class AIxEnumDataRegistryIllegalOperationException(RuntimeError):
    def __init__(self, aMessage): super().__init__(aMessage() if callable(aMessage) else aMessage)
