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
        .appendTo(td);

      $('<div>')
        .addClass('init-time')
        .css('width', `${ init / highestCumulative * 100 }%`)
        .appendTo(meter);

      $('<div>')
        .addClass('exec-time')
        .css('width', `${ exec / highestCumulative * 100 }%`)
        .appendTo(meter);
    });
  });
