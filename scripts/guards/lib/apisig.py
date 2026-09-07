"""Reader for the committed Metalava signature files in api/.

api_doc_coverage.py and api_test_presence.py both take their definition of
"what is public" from these files — the same source public-api-baseline
checks — so the three guards can never disagree about the API surface.

Format (Metalava signature format 4.0):

    package org.prebid.mobile {
      public class AdSize {
        ctor public AdSize(int, int);
        method public int getHeight();
      }
      public enum NativeImage.Type { … }        # nested type, dotted name
    }
"""

import os
import re

_PACKAGE_RE = re.compile(r"^package\s+([\w.]+)\s*\{")
# A type header line: modifiers then class/interface/enum/@interface then the
# (possibly dotted, for nested types) name.
_TYPE_RE = re.compile(
    r"^\s+public\s+(?:[\w-]+\s+)*(class|interface|enum|@interface|annotation)\s+"
    r"([\w.]+)")


class SignatureError(Exception):
    """A signature file is missing or unreadable."""


def public_types(path):
    """[(qualified_name, kind)] for every public type in one signature file.

    Nested types keep their dotted name (org.prebid.mobile.NativeImage.Type).
    """
    if not os.path.exists(path):
        raise SignatureError(f"signature file {path} is missing")
    types = []
    package = None
    with open(path, encoding="utf-8", errors="replace") as fh:
        for line in fh:
            package_match = _PACKAGE_RE.match(line)
            if package_match:
                package = package_match.group(1)
                continue
            type_match = _TYPE_RE.match(line)
            if type_match and package:
                kind, name = type_match.groups()
                types.append((f"{package}.{name}", kind))
    return types


def simple_name(qualified_name):
    """The last dotted segment: the name tests reference."""
    return qualified_name.rsplit(".", 1)[-1]


def outer_source_candidates(qualified_name, package_of):
    """Relative source paths (under a module's java root) that could define
    the type: the OUTER class's .java/.kt file."""
    package = package_of
    remainder = qualified_name[len(package) + 1:]
    outer = remainder.split(".")[0]
    base = os.path.join(*package.split("."), outer)
    return [base + ".java", base + ".kt"]


def package_of(qualified_name, known_packages):
    """The longest known package that prefixes the qualified name."""
    best = ""
    for package in known_packages:
        if qualified_name.startswith(package + ".") and len(package) > len(best):
            best = package
    return best or qualified_name.rsplit(".", 1)[0]


def packages(path):
    """Every package declared in one signature file."""
    result = []
    with open(path, encoding="utf-8", errors="replace") as fh:
        for line in fh:
            match = _PACKAGE_RE.match(line)
            if match:
                result.append(match.group(1))
    return result
