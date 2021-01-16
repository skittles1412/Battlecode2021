// quick scraper to get scrim info
// Google Sheets doesn't recognize and separate the formulas
// So after pasting and separating by column, do a find/replace for "EQUALS" -> "=" and "COMMA" -> ","

let out = [];
for (const row of document.querySelectorAll("tbody > tr")) {
    const cols = row.querySelectorAll("td");
    const time = cols[0].textContent + ' ' + cols[1].textContent;
    const score = cols[3].textContent;
    const opp = cols[4].textContent;
    const rank = cols[5].textContent;
    let linkedTime = time;
    if (cols[6].textContent != "N/A") {
         const replay = cols[6].querySelector('a').href;
         linkedTime = "EQUALSHYPERLINK(\"" + replay + "\"COMMA\"" + time + "\")";
    }
    const elems = [linkedTime, opp, score.length == 3 ? "Queued" : score, rank];
    out.push(elems.join());
}
out.reverse();
console.log(out.join('\n'));
