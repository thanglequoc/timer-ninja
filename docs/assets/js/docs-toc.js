// ==============================================
// Docs TOC — Timer Ninja
// Auto-generate sidebar TOC from page headings + scrollspy
// ==============================================

(function () {
  'use strict';

  var tocContainer = document.getElementById('docsToc');
  var content = document.querySelector('.docs__content');
  if (!tocContainer || !content) return;

  // Gather h2 and h3 headings
  var headings = content.querySelectorAll('h2, h3');
  if (!headings.length) return;

  var tocHTML = '';
  var currentH2 = null;

  headings.forEach(function (heading) {
    // Ensure heading has an ID
    if (!heading.id) {
      heading.id = heading.textContent
        .toLowerCase()
        .replace(/[^\w\s-]/g, '')
        .replace(/\s+/g, '-')
        .replace(/-+/g, '-')
        .trim();
    }

    var text = heading.textContent;
    var id = heading.id;

    if (heading.tagName === 'H2') {
      if (currentH2) {
        tocHTML += '</ul></li>';
      }
      tocHTML += '<li><a href="#' + id + '">' + text + '</a><ul>';
      currentH2 = heading;
    } else if (heading.tagName === 'H3') {
      tocHTML += '<li><a href="#' + id + '">' + text + '</a></li>';
    }
  });

  if (currentH2) {
    tocHTML += '</ul></li>';
  }

  tocContainer.innerHTML = tocHTML;

  // Scrollspy
  var tocLinks = tocContainer.querySelectorAll('a');
  var headingElements = Array.from(headings);

  function updateActiveLink() {
    var scrollPos = window.scrollY + 100;
    var activeId = '';

    for (var i = headingElements.length - 1; i >= 0; i--) {
      if (headingElements[i].offsetTop <= scrollPos) {
        activeId = headingElements[i].id;
        break;
      }
    }

    tocLinks.forEach(function (link) {
      var href = link.getAttribute('href').substring(1);
      if (href === activeId) {
        link.classList.add('is-active');
      } else {
        link.classList.remove('is-active');
      }
    });
  }

  window.addEventListener('scroll', updateActiveLink, { passive: true });
  updateActiveLink();
})();
