"""Shared pytest fixtures and sys.path bootstrap for the xiaozhi-server tests.

The production entrypoint (app.py) expects `main/xiaozhi-server` on the Python
path so that top-level packages like `config`, `core`, and `plugins_func`
resolve. Replicating that here lets tests import them without requiring users
to `cd` into the directory first, and without installing the package.
"""

from __future__ import annotations

import os
import sys
from pathlib import Path

import pytest

# Make main/xiaozhi-server importable.
ROOT = Path(__file__).resolve().parent.parent
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))


@pytest.fixture()
def project_root() -> Path:
    """Absolute path to the xiaozhi-server project root."""
    return ROOT


@pytest.fixture()
def restore_cwd():
    """Restore the working directory after a test that chdirs."""
    original = os.getcwd()
    yield
    os.chdir(original)
