// ==============================================
// Theme Toggle — Timer Ninja
// Day/Night mode with localStorage persistence
// ==============================================

(function () {
  'use strict';

  var STORAGE_KEY = 'timer-ninja-theme';
  var toggle = document.getElementById('themeToggle');
  var html = document.documentElement;

  function getPreferredTheme() {
    var stored = localStorage.getItem(STORAGE_KEY);
    if (stored) return stored;
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }

  function setTheme(theme) {
    html.setAttribute('data-theme', theme);
    localStorage.setItem(STORAGE_KEY, theme);
  }

  // Initialize
  setTheme(getPreferredTheme());

  // Toggle
  if (toggle) {
    toggle.addEventListener('click', function () {
      var current = html.getAttribute('data-theme');
      setTheme(current === 'dark' ? 'light' : 'dark');
    });
  }

  // Listen for system preference changes
  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', function (e) {
    if (!localStorage.getItem(STORAGE_KEY)) {
      setTheme(e.matches ? 'dark' : 'light');
    }
  });

  // Hamburger menu
  var hamburger = document.getElementById('navHamburger');
  var navLinks = document.getElementById('navLinks');

  if (hamburger && navLinks) {
    hamburger.addEventListener('click', function () {
      hamburger.classList.toggle('is-open');
      navLinks.classList.toggle('is-open');
    });

    // Close on link click (mobile)
    navLinks.querySelectorAll('.navbar__link').forEach(function (link) {
      link.addEventListener('click', function () {
        hamburger.classList.remove('is-open');
        navLinks.classList.remove('is-open');
      });
    });
  }

  // Navbar background on scroll + back-to-top
  var navbar = document.getElementById('navbar');
  var backToTop = document.getElementById('backToTop');

  if (navbar || backToTop) {
    window.addEventListener('scroll', function () {
      var scrolled = window.scrollY > 50;
      if (navbar) {
        navbar.style.boxShadow = scrolled ? '0 2px 20px rgba(0,0,0,0.08)' : 'none';
      }
      if (backToTop) {
        if (window.scrollY > 400) {
          backToTop.classList.add('is-visible');
        } else {
          backToTop.classList.remove('is-visible');
        }
      }
    }, { passive: true });
  }

  if (backToTop) {
    backToTop.addEventListener('click', function () {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
  }
})();
