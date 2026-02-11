// ==============================================
// Typing Effect — Timer Ninja
// Animated code typing for comparison section
// ==============================================

(function () {
  'use strict';

  var codeTraditional = document.getElementById('codeTraditional');
  var codeTimerNinja = document.getElementById('codeTimerNinja');

  if (!codeTraditional || !codeTimerNinja) return;

  function animateTyping(element, text, speed) {
    element.textContent = '';
    element.style.visibility = 'visible';

    var i = 0;
    var cursor = document.createElement('span');
    cursor.className = 'typing-cursor';
    element.appendChild(cursor);

    return new Promise(function (resolve) {
      function type() {
        if (i < text.length) {
          element.insertBefore(
            document.createTextNode(text.charAt(i)),
            cursor
          );
          i++;
          setTimeout(type, speed);
        } else {
          setTimeout(function () {
            if (cursor.parentNode) {
              cursor.parentNode.removeChild(cursor);
            }
            // Restore text and apply Prism highlighting
            element.textContent = text;
            if (window.Prism) {
              Prism.highlightElement(element);
            }
            resolve();
          }, 600);
        }
      }
      type();
    });
  }

  // Store original text
  var traditionalText = codeTraditional.textContent;
  var ninjaText = codeTimerNinja.textContent;

  // Only run animation once when section is visible
  var comparisonSection = document.getElementById('comparison');
  if (!comparisonSection) return;

  var hasAnimated = false;

  var observer = new IntersectionObserver(function (entries) {
    entries.forEach(function (entry) {
      if (entry.isIntersecting && !hasAnimated) {
        hasAnimated = true;
        observer.unobserve(entry.target);

        // Start typing animation on both panels
        codeTraditional.textContent = '';
        codeTimerNinja.textContent = '';

        setTimeout(function () {
          animateTyping(codeTraditional, traditionalText, 18).then(function () {
            return animateTyping(codeTimerNinja, ninjaText, 22);
          });
        }, 400);
      }
    });
  }, {
    threshold: 0.4
  });

  observer.observe(comparisonSection);
})();
