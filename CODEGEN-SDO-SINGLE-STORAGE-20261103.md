# SDO jedno pořadově zachovávané úložiště – stav ze dne 2026-11-03

- Java i Python ukládají deklarované hodnoty a `additionalProperties` do jedné mapy v pořadí vložení; nepřítomnost je klíč chybějící v mapě, explicitní `null` je přítomná hodnota.
- Interní a dodatečné klíče mají oddělený jmenný prostor. Rezervované externí názvy a konfigurovaná schema metadata nelze přepsat v dodatečné mapě.
- Dodatečné vlastnosti tvoří živý typovaný mapový pohled s kontrolou typu a atomickým hromadným zápisem.
- JSON/YAML renderer i virtuální mapování v Javě čtou stejné pořadí ze snapshotu; Python JSON/YAML renderer a `to_mapping` rovněž.
- XMLDEFS→integrovaný MAP je odložený: při požadavku na XML serializaci s dodatečnými vlastnostmi se místo ztráty dat vrací explicitní chyba.
- Nová sada Python testů a původní SDO Python testy: **60 passed** (součást 99 kombinovaných testů); Java SDO TestNG scénáře: **4 passed** v lokálním in-process běhu.

Zbývá zapojit nový codegen model do produkčního SDO generátoru, dokončit round-trip parsování integrated property maps, a otestovat všechny varianty serializačních profilů. Překlad cílených Java modulů prošel s pouze dočasnou náhradou závislosti na anotaci `org.jetbrains.annotations.NotNull`; oficiální Gradle build nebyl k dispozici.
