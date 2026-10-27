"""Offline integrity tests for the Maven staging/promotion protocol."""
import json
from pathlib import Path
import tempfile
import unittest
import xml.etree.ElementTree as ET
import importlib.util

spec=importlib.util.spec_from_file_location('stage',Path(__file__).with_name('bootstrap-maven-stage.py'))
stage=importlib.util.module_from_spec(spec)
spec.loader.exec_module(stage)

class MavenStageTests(unittest.TestCase):
    def setUp(self):
        self.tmp=tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root=Path(self.tmp.name)
        self.staging=self.root/'stage'
        self.local=self.root/'m2'
        self.group='eu.algites.demo';self.artifact='demo-native';self.version='1.1-SNAPSHOT'
        self.directory=self.staging/'eu/algites/demo'/self.artifact/self.version
        self.directory.mkdir(parents=True)
        self.coordinates=self.root/'coords.tsv';self.coordinates.write_text(f'{self.group}\t{self.artifact}\t{self.version}\n')
        self.manifest=self.root/'manifest.json'
        self.local_manifest=self.root/'local.tsv'
    def prepare(self, name=None, source=True):
        name=name or f'{self.artifact}-{self.version}'
        for suffix in ('.jar','.module','-sources.jar'):
            if suffix == '-sources.jar' and not source: continue
            (self.directory/(name+suffix)).write_bytes((b'// Gradle metadata header\n' + json.dumps({'formatVersion':'1.1','variants':[{'files':[{'name':name+'.jar','url':name+'.jar'}]}]}).encode()) if suffix=='.module' else b'tested immutable bytes '+suffix.encode())
        (self.directory/(name+'.pom')).write_text(f'<project><groupId>{self.group}</groupId><artifactId>{self.artifact}</artifactId><version>{self.version}</version></project>')
    def test_logical_snapshot_and_promotion(self):
        self.prepare()
        stage.collect(self.staging,self.coordinates,self.manifest)
        stage.promote(self.manifest,self.local,self.local_manifest)
        rows=self.local_manifest.read_text().splitlines()[0].split('\t')
        self.assertEqual(len(rows),4)
        self.assertEqual(stage.digest(Path(rows[0])),stage.digest(self.directory/(self.artifact+'-'+self.version+'.jar')))
        self.assertIn('1.1-SNAPSHOT',(self.local/'eu/algites/demo'/self.artifact/'maven-metadata-local.xml').read_text())
    def test_timestamped_snapshot(self):
        name=self.artifact+'-1.1-20261026.183000-3'
        self.prepare(name)
        root=ET.Element('metadata')
        for key,value in (('groupId',self.group),('artifactId',self.artifact),('version',self.version)):
            ET.SubElement(root,key).text=value
        v=ET.SubElement(root,'versioning')
        svs=ET.SubElement(v,'snapshotVersions')
        for ext, classifier in (('jar',''),('pom',''),('module',''),('jar','sources')):
            sv=ET.SubElement(svs,'snapshotVersion')
            ET.SubElement(sv,'extension').text=ext
            if classifier: ET.SubElement(sv,'classifier').text=classifier
            ET.SubElement(sv,'value').text='1.1-20261026.183000-3'
        ET.ElementTree(root).write(self.directory/'maven-metadata.xml',encoding='unicode')
        stage.collect(self.staging,self.coordinates,self.manifest)
        stage.promote(self.manifest,self.local,self.local_manifest)
        self.assertIn('demo-native-1.1-SNAPSHOT.jar',self.local_manifest.read_text())
    def test_module_references_use_local_snapshot_names(self):
        timestamped = self.artifact + '-1.1-20261026.183000-3'
        self.prepare(timestamped)
        root=ET.Element('metadata')
        for key,value in (('groupId',self.group),('artifactId',self.artifact),('version',self.version)):
            ET.SubElement(root,key).text=value
        v=ET.SubElement(root,'versioning')
        svs=ET.SubElement(v,'snapshotVersions')
        for ext, classifier in (('jar',''),('pom',''),('module',''),('jar','sources')):
            sv=ET.SubElement(svs,'snapshotVersion')
            ET.SubElement(sv,'extension').text=ext
            if classifier: ET.SubElement(sv,'classifier').text=classifier
            ET.SubElement(sv,'value').text='1.1-20261026.183000-3'
        ET.ElementTree(root).write(self.directory/'maven-metadata.xml',encoding='unicode')
        stage.collect(self.staging,self.coordinates,self.manifest)
        stage.promote(self.manifest,self.local,self.local_manifest)
        module=Path(self.local_manifest.read_text().splitlines()[0].split('\t')[2])
        metadata=module.read_text()
        self.assertIn('demo-native-1.1-SNAPSHOT.jar',metadata)
        self.assertNotIn('demo-native-1.1-20261026.183000-3.jar',metadata)
    def test_collect_existing_tsv(self):
        self.prepare()
        row='\t'.join(str(p) for p in stage.artifact_files(self.directory,self.artifact,self.version))
        input_file=self.root/'original.tsv'
        input_file.write_text(row+'\n')
        stage.collect_tsv(input_file,self.manifest)
        self.assertEqual(len(json.loads(self.manifest.read_text())),1)
    def test_reject_changes_before_install(self):
        self.prepare()
        stage.collect(self.staging,self.coordinates,self.manifest)
        (self.directory/(self.artifact+'-'+self.version+'.jar')).write_bytes(b'corrupt')
        with self.assertRaisesRegex(ValueError,'changed after verification'):
            stage.promote(self.manifest,self.local,self.local_manifest)
        self.assertFalse(self.local.exists())
    def test_sources_optional(self):
        self.prepare(source=False)
        stage.collect(self.staging,self.coordinates,self.manifest)
        stage.promote(self.manifest,self.local,self.local_manifest)
        self.assertEqual(self.local_manifest.read_text().split('\t')[-1].strip(),'')
    def test_wrong_pom_fails_before_promote(self):
        self.prepare()
        pom=self.directory/(self.artifact+'-'+self.version+'.pom')
        pom.write_text(pom.read_text().replace('<groupId>eu.algites.demo</groupId>','<groupId>other</groupId>'))
        with self.assertRaisesRegex(ValueError,'POM'):
            stage.collect(self.staging,self.coordinates,self.manifest)

if __name__=='__main__': unittest.main()
