const updateMeters = () => {
  $('tr:not(:first-child)')
    .each((i, tr) => {
      let highestCumulative = 0;
      let lowestCumulative = Number.MAX_SAFE_INTEGER;

      $(tr).find('td:visible').each((i, td) => {
        const init = parseFloat($(td).attr("data-init"));
        const exec = parseFloat($(td).attr("data-exec"));

        if (init + exec > highestCumulative) {
          highestCumulative = init + exec;
        }

        if (init + exec !== 0 && init + exec < lowestCumulative) {
          lowestCumulative = init + exec;
        }
      });

      $(tr).find('td').each((i, td) => {
        const init = parseFloat($(td).attr("data-init"));
        const exec = parseFloat($(td).attr("data-exec"));

        let meter = $(td).find('.meter');
        let init_time_el = $(td).find('.meter .init-time');
        let exec_time_el = $(td).find('.meter .exec-time');
        let empty_el = $(td).find('.meter .meter-empty');

        if (!meter.length) {
          meter = $('<div>')
            .addClass('meter')
            .appendTo($(td).find('.cell-wrapper'));

          init_time_el = $('<div>')
            .addClass('init-time')
            .appendTo(meter);

          exec_time_el = $('<div>')
            .addClass('exec-time')
            .appendTo(meter);

          empty_el = $('<div>')
            .addClass('meter-empty')
            .appendTo(meter);
        }

        $(td).off().hover(
          () => empty_el.css('flex-grow', 0),
          () => empty_el.css('flex-grow', 1000 - (init + exec) / highestCumulative * 1000),
        );

        init_time_el.css('flex-grow', init / highestCumulative * 1000);
        exec_time_el.css('flex-grow', exec / highestCumulative * 1000);
        empty_el.css('flex-grow', 1000 - (init + exec) / highestCumulative * 1000);

        if (exec + init === 0) {
          $(td).css('opacity', 0.3);
        } else if (exec + init == highestCumulative) {
          $(td).css('background-color', '#f001');
        } else if (exec + init == lowestCumulative) {
          $(td).css('background-color', '#0f01');
        } else {
          $(td).css('background-color', 'transparent');
        }
      });
    });
};

updateMeters();

const algoList = $('<ul>').appendTo(document.body);

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
