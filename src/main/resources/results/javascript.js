$('tr:not(:first-child)')
  .each((i, tr) => {
    let highestCumulative = 0;

    $(tr).find('td').each((i, td) => {
      const init = parseFloat($(td).attr("data-init"));
      const exec = parseFloat($(td).attr("data-exec"));

      if (init + exec > highestCumulative) {
        highestCumulative = init + exec;
      }
    });

    $(tr).find('td').each((i, td) => {
      const init = parseFloat($(td).attr("data-init"));
      const exec = parseFloat($(td).attr("data-exec"));

      const meter = $('<div>')
        .addClass('meter')
        .appendTo($(td).find('.cell-wrapper'));

      $('<div>')
        .addClass('init-time')
        .css('flex-grow', init / highestCumulative * 1000)
        .appendTo(meter);

      $('<div>')
        .addClass('exec-time')
        .css('flex-grow', exec / highestCumulative * 1000)
        .appendTo(meter);

      const empty = $('<div>')
        .addClass('meter-empty')
        .css('flex-grow', 1000 - (init + exec) / highestCumulative * 1000)
        .appendTo(meter);

      $(td).hover(
        () => empty.css('flex-grow', 0),
        () => empty.css('flex-grow', 1000 - (init + exec) / highestCumulative * 1000),
      );
    });
  });
