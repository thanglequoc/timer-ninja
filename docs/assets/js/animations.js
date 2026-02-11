// ==============================================
// Scroll Animations — Timer Ninja
// IntersectionObserver-based scroll reveals
// ==============================================

(function () {
  'use strict';

  var ANIMATED_CLASS = 'is-animated';
  var SELECTORS = [
    '.animate-fade-in',
    '.animate-fade-up',
    '.animate-fade-left',
    '.animate-fade-right',
    '.animate-scale-up'
  ].join(',');

  function initAnimations() {
    var elements = document.querySelectorAll(SELECTORS);
    if (!elements.length) return;

    // Feature check
    if (!('IntersectionObserver' in window)) {
      elements.forEach(function (el) {
        el.classList.add(ANIMATED_CLASS);
      });
      return;
    }

    var observer = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          entry.target.classList.add(ANIMATED_CLASS);
          observer.unobserve(entry.target);
        }
      });
    }, {
      root: null,
      rootMargin: '0px 0px -60px 0px',
      threshold: 0.1
    });

    elements.forEach(function (el) {
      observer.observe(el);
    });
  }

  // Parallax effect for hero + scroll hint fade-out
  function initParallax() {
    var hero = document.querySelector('.hero');
    var mascot = document.querySelector('.hero__mascot');
    var scrollHint = document.querySelector('.hero__scroll-hint');
    if (!hero || !mascot) return;

    window.addEventListener('scroll', function () {
      var scrollY = window.scrollY;
      var heroHeight = hero.offsetHeight;

      if (scrollY < heroHeight) {
        var progress = scrollY / heroHeight;
        mascot.style.transform = 'translateY(' + (scrollY * 0.15) + 'px)';
        hero.style.backgroundPositionY = (progress * 30) + '%';
      }

      // Fade out scroll hint after 120px
      if (scrollHint) {
        var hintOpacity = Math.max(0, 0.6 - scrollY / 200);
        scrollHint.style.opacity = hintOpacity;
        scrollHint.style.pointerEvents = hintOpacity < 0.1 ? 'none' : '';
      }
    }, { passive: true });
  }

  // Trace output line-by-line reveal
  function initTraceAnimation() {
    var traceBody = document.getElementById('traceBody');
    if (!traceBody) return;

    var lines = traceBody.querySelectorAll('.trace-line');
    if (!lines.length) return;

    var observer = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          lines.forEach(function (line, index) {
            setTimeout(function () {
              line.classList.add('is-visible');
            }, index * 120);
          });
          observer.unobserve(entry.target);
        }
      });
    }, {
      threshold: 0.3
    });

    observer.observe(traceBody);
  }

  // Tab switcher
  function initTabs() {
    document.querySelectorAll('.tab-switcher').forEach(function (switcher) {
      var buttons = switcher.querySelectorAll('.tab-switcher__btn');
      buttons.forEach(function (btn) {
        btn.addEventListener('click', function () {
          var tabId = btn.getAttribute('data-tab');
          var parent = switcher.parentElement;

          // Update buttons
          buttons.forEach(function (b) { b.classList.remove('is-active'); });
          btn.classList.add('is-active');

          // Update content
          parent.querySelectorAll('.tab-content').forEach(function (tc) {
            tc.classList.remove('is-active');
          });
          var target = document.getElementById(tabId);
          if (target) target.classList.add('is-active');
        });
      });
    });
  }

  // Init all
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function () {
      initAnimations();
      initParallax();
      initTraceAnimation();
      initTabs();
    });
  } else {
    initAnimations();
    initParallax();
    initTraceAnimation();
    initTabs();
  }
})();
