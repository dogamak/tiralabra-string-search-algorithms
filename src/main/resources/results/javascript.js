let comparisonRef = { value: 'time' };

const setComparison = (value) => {
    comparisonRef.value = value;
    updateMeters();
};

const updateMeters = () => {
  let meterSegments = {
    time: [
        {
            className: 'exec-time',
            get: (td) => parseFloat($(td).attr('data-exec')),
        },
        {
            className: 'init-time',
            get: (td) => parseFloat($(td).attr('data-init')),
        }
    ],
    speed: [{
        className: 'speed',
        get: (td) => parseFloat($(td).attr('data-speed')),
    }],
  }[comparisonRef.value];

  let lowerBetter = true;

  if (comparisonRef.value == 'speed') {
    lowerBetter = false;
  }

  $('tr:not(:first-child)')
    .each((i, tr) => {
      let highestCumulative = 0;
      let lowestCumulative = Number.MAX_SAFE_INTEGER;

      $(tr).find('td:visible').each((i, td) => {
        let cumulative = 0;

        for (const { get } of meterSegments) {
            cumulative += get(td);
        }

        if (cumulative > highestCumulative) {
          highestCumulative = cumulative;
        }

        if (cumulative !== 0 && cumulative < lowestCumulative) {
          lowestCumulative = cumulative;
        }
      });

      $(tr).find('td').each((i, td) => {
        let meter = $(td).find('.meter');
        meter.html('');
        meter = $(td).find('meter');

        let empty_el = $(td).find('.meter .meter-empty');

        if (!meter.length) {
          meter = $('<div>')
            .addClass('meter')
            .appendTo($(td).find('.cell-wrapper'));

          for (const { className } of meterSegments) {
              $('<div>')
                  .addClass(className)
                  .appendTo(meter);
          }

          empty_el = $('<div>')
            .addClass('meter-empty')
            .appendTo(meter);
        }

        let cumulative = 0;

        for (const { get, className } of meterSegments) {
            let value = get(td);
            cumulative += value;
            $(td).find('.' + className).css('flex-grow', value / highestCumulative * 1000);
        }

        $(td).off().hover(
          () => empty_el.css('flex-grow', 0),
          () => empty_el.css('flex-grow', 1000 - cumulative / highestCumulative * 1000),
        );

        empty_el.css('flex-grow', 1000 - cumulative / highestCumulative * 1000);

        let bestValue = lowestCumulative;
        let worstValue = highestCumulative;

        if (!lowerBetter) {
            bestValue = highestCumulative;
            worstValue = lowestCumulative;
        }

        if (cumulative === 0) {
          $(td).css('opacity', 0.3);
        } else if (cumulative == worstValue) {
          $(td).css('background-color', '#f002');
        } else if (cumulative == bestValue) {
          $(td).css('background-color', '#0f02');
        } else {
          $(td).css('background-color', 'transparent');
        }
      });
    });
};

updateMeters();

const rightDiv = $('<div>')
    .addClass('options')
    .append($('<h4>Algorithms</h4>'))
    .appendTo(document.body);

const algoList = $('<ul>').appendTo(rightDiv);

$('tr:first-child th:not(:first-child)').each((i, th) => {
  const algorithm = $(th).text();

  const li = $('<li>').appendTo(algoList);

  $('<input>')
    .attr('checked', true)
    .attr('type', 'checkbox')
    .appendTo(li)
    .change((evt) => {
      const value = $(evt.target).prop('checked');

      $(`tr > *:nth-child(${i + 2})`).toggle(value);

      updateMeters();
    });

  $('<span>')
    .text(algorithm)
    .appendTo(li);
});

$('<h4>Compare</h4>').appendTo(rightDiv);

$('<ul>')
    .append($('<li>').append($('<input>').attr('checked', true).attr('type', 'radio').attr('name', 'cmp').click(() => setComparison('time'))).append($('<span>').text('Iteration time')))
    .append($('<li>').append($('<input>').attr('checked', false).attr('type', 'radio').attr('name', 'cmp').click(() => setComparison('speed'))).append($('<span>').text('Speed')))
    .appendTo(rightDiv);