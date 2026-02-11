// ==============================================
// Prism.js Init — Timer Ninja
// Bridges Jekyll/Rouge code blocks to Prism.js
// and syncs Prism theme with day/night mode
// ==============================================

(function () {
  'use strict';

  // Rouge (Jekyll's highlighter) adds classes like `language-java` or
  // `highlight` with nested `<code>` blocks. Prism expects
  // `<code class="language-xxx">` inside a `<pre>`.
  // This script normalizes Rouge output for Prism compatibility.

  function bridgeRougeToprism() {
    // Handle Rouge-generated blocks: <div class="language-java highlighter-rouge">
    document.querySelectorAll('div[class*="language-"]').forEach(function (div) {
      var classes = div.className.split(/\s+/);
      var lang = '';
      classes.forEach(function (cls) {
        var match = cls.match(/^language-(.+)$/);
        if (match) lang = match[1];
      });

      if (!lang) return;

      var pre = div.querySelector('pre');
      var code = div.querySelector('code');
      if (pre && code) {
        code.className = 'language-' + lang;
        pre.className = 'language-' + lang;
      }
    });

    // Handle plain markdown ```java blocks that Jekyll may render as
    // <pre><code class="language-java">
    document.querySelectorAll('pre code[class*="language-"]').forEach(function (code) {
      var pre = code.parentElement;
      if (pre && pre.tagName === 'PRE' && !pre.className.match(/language-/)) {
        var langClass = code.className.match(/language-\S+/);
        if (langClass) {
          pre.classList.add(langClass[0]);
        }
      }
    });

    // Handle code without language class — treat as plain text
    document.querySelectorAll('pre code:not([class*="language-"])').forEach(function (code) {
      code.classList.add('language-none');
    });
  }

  // Sync Prism stylesheet with current theme
  function syncPrismTheme() {
    var theme = document.documentElement.getAttribute('data-theme');
    var lightSheet = document.getElementById('prism-light');
    var darkSheet = document.getElementById('prism-dark');

    if (lightSheet && darkSheet) {
      lightSheet.disabled = (theme === 'dark');
      darkSheet.disabled = (theme !== 'dark');
    }
  }

  // Watch for theme changes (from theme-toggle.js)
  var observer = new MutationObserver(function (mutations) {
    mutations.forEach(function (mutation) {
      if (mutation.attributeName === 'data-theme') {
        syncPrismTheme();
      }
    });
  });

  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['data-theme']
  });

  // Initialize
  function init() {
    bridgeRougeToprism();
    syncPrismTheme();

    // Re-highlight with Prism
    if (window.Prism) {
      Prism.highlightAll();
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
