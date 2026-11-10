"""Unit tests for the manifest-surface probe."""

import os
import shutil
import sys
import tempfile
import unittest

_HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(_HERE, "..", "checks"))
sys.path.insert(0, os.path.join(_HERE, "..", "lib"))

import guardlib  # noqa: E402
import manifest_surface  # noqa: E402

NS = 'xmlns:android="http://schemas.android.com/apk/res/android"'


class ManifestSurfaceTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.mkdtemp(prefix="manifest-test-")
        self.addCleanup(shutil.rmtree, self.tmp)

    def write_manifest(self, module, body):
        path = os.path.join(self.tmp, "PrebidMobile", module, "src", "main",
                            "AndroidManifest.xml")
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", encoding="utf-8") as fh:
            fh.write(f"<manifest {NS}>{body}</manifest>")

    def test_permissions_features_and_library(self):
        self.write_manifest("PrebidMobile-core", """
            <uses-permission android:name="android.permission.INTERNET"/>
            <uses-permission-sdk-23 android:name="android.permission.CAMERA"/>
            <uses-feature android:name="android.hardware.wifi" android:required="false"/>
            <application>
              <uses-library android:name="org.apache.http.legacy" android:required="false"/>
            </application>""")
        self.assertEqual(manifest_surface.surface(self.tmp), [
            "feature:android.hardware.wifi required=false",
            "permission:android.permission.CAMERA (sdk-23)",
            "permission:android.permission.INTERNET",
            "uses-library:org.apache.http.legacy required=false",
        ])

    def test_only_exported_true_components_snapshotted(self):
        self.write_manifest("PrebidMobile-core", """
            <application>
              <activity android:name=".Hidden" android:exported="false"/>
              <activity android:name=".Open" android:exported="true"/>
              <service android:name=".Svc" android:exported="true"/>
            </application>""")
        self.assertEqual(manifest_surface.surface(self.tmp), [
            "exported:activity:.Open",
            "exported:service:.Svc",
        ])

    def test_queries_entries_snapshotted(self):
        self.write_manifest("PrebidMobile-core", """
            <queries>
              <package android:name="com.example.store"/>
              <intent><action android:name="android.intent.action.VIEW"/></intent>
            </queries>""")
        self.assertEqual(manifest_surface.surface(self.tmp), [
            "queries:intent:android.intent.action.VIEW",
            "queries:package:com.example.store",
        ])

    def test_duplicate_entries_across_modules_merge(self):
        body = '<uses-permission android:name="android.permission.INTERNET"/>'
        self.write_manifest("PrebidMobile-core", body)
        self.write_manifest("PrebidMobile-gamEventHandlers", body)
        self.assertEqual(manifest_surface.surface(self.tmp),
                         ["permission:android.permission.INTERNET"])

    def test_broken_manifest_fails_not_empty(self):
        path = os.path.join(self.tmp, "PrebidMobile", "PrebidMobile-core",
                            "src", "main", "AndroidManifest.xml")
        os.makedirs(os.path.dirname(path))
        with open(path, "w") as fh:
            fh.write("<manifest unclosed")
        with self.assertRaises(guardlib.GuardDataError):
            manifest_surface.surface(self.tmp)

    def test_no_manifests_fails_not_empty(self):
        with self.assertRaises(guardlib.GuardDataError):
            manifest_surface.surface(self.tmp)


if __name__ == "__main__":
    unittest.main()
