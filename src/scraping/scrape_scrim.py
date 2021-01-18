from collections import defaultdict
from time import sleep

from selenium import webdriver
from selenium.common.exceptions import NoSuchElementException, StaleElementReferenceException
from selenium.webdriver.common.by import By

CREDENTIALS = open("credentials.txt", "r").readlines()
USERNAME = CREDENTIALS[0].strip()
PASSWORD = CREDENTIALS[1].strip()
PAGES = 10


class DashboardScraper:
    def __init__(self):
        self.browser = webdriver.Chrome()

    def run(self):
        self.browser.get("https://2021.battlecode.org/login")
        self.login()
        while self.browser.current_url.endswith("login"):
            sleep(0.1)

        self.browser.get("https://2021.battlecode.org/scrimmaging")

        result = []
        links = []
        for page in range(1, PAGES + 1):
            for attempt in range(100):
                sleep(0.1)
                try:
                    self.browser.find_element(By.XPATH, f'//a[@class="page-link"][text()="{page}"]').click()
                    scrims = self.scrape()
                    result.extend(scrims[0])
                    links.extend(scrims[1])
                    if len(scrims) == 0:
                        print(f"Page {page} has no finished scrims found")
                    break
                except (NoSuchElementException, StaleElementReferenceException):
                    pass

        self.browser.quit()

        return result[::-1], links[::-1]

    def login(self):
        username_box = self.browser.find_element(By.ID, "username")
        password_box = self.browser.find_element(By.ID, "password")
        username_box.send_keys(USERNAME)
        password_box.send_keys(PASSWORD)
        self.browser.find_element(By.CSS_SELECTOR, 'button[type="submit"]').click()

    def scrape(self):
        table_body = self.browser.find_element(By.CSS_SELECTOR, "table.table tbody")
        rows = table_body.find_elements(By.CSS_SELECTOR, "tr")

        scrims = []
        links = []
        for row in rows:
            cols = row.find_elements(By.CSS_SELECTOR, "td")
            assert len(cols) == 7

            time = cols[0].text + ' ' + cols[1].text
            if cols[2].text in ("Rejected", "Error", "Pending"):
                continue
            score = cols[3].text
            opp = cols[4].text
            rank = cols[5].text
            linked_time = time
            links.append("N/A")
            if cols[6].text != "N/A":
                links[-1] = cols[6].find_element(By.CSS_SELECTOR, 'a').get_attribute("href")
                linked_time = "EQUALSHYPERLINK(\"" + links[-1] + "\"COMMA\"" + time + "\")"

            elems = [linked_time, opp, score if len(score) > 3 else "N/A", rank]
            scrims.append(','.join(elems))
        return scrims, links


class MatchScraper:
    def __init__(self, browser, link):
        self.browser = browser
        self.browser.get(link)

    def exists(self, by: By, selector: str) -> bool:
        try:
            self.browser.find_element(by, selector)
            return True
        except NoSuchElementException:
            return False

    def run(self):
        while not self.exists(By.CSS_SELECTOR, "div.gameWrapper"):
            self.browser.find_element(By.XPATH, '//button[contains(@class, "modebutton")][text()="Queue"]').click()
            sleep(0.1)

        teams = self.browser.find_element(By.CSS_SELECTOR, "span.red").text.strip(), self.browser.find_element(
            By.CSS_SELECTOR, "span.blue").text.strip()
        opponent = teams[0] if teams[0] != "java :ghosthug:" else teams[1]

        maps = []
        durations = []
        winners = []
        for i in range(3):
            info = self.browser.find_element(By.CSS_SELECTOR,
                                             f"div.gameWrapper > div:nth-of-type({i + 1})").text  # 1-indexed
            round_map = info.split('-')[0]
            duration = info.strip().split()[-2]
            maps.append(round_map.strip())
            durations.append(duration.strip())
            winners.append(info.split('-', maxsplit=1)[1].rsplit(" wins ", maxsplit=1)[0].strip())

        return opponent, maps, durations, winners


def main():
    scrims, links = DashboardScraper().run()
    browser = webdriver.Chrome()

    output = []
    team_record = defaultdict(lambda: [0, 0])
    map_record = defaultdict(lambda: [0, 0])
    for scrim, link in zip(scrims, links):
        if link.startswith("https://"):
            opponent, maps, durations, winners = MatchScraper(browser, link).run()
            for m, d, w in zip(maps, durations, winners):
                idx = w != "java :ghosthug:"
                team_record[opponent][idx] += 1
                map_record[m][idx] += 1
        else:
            maps, durations, winners = [["N/A"] * 3] * 3
        output.append(scrim)
        for i in range(3):
            output.append(f"Round {i + 1},{maps[i]},{durations[i]},{winners[i]}")
        output.append('')

    browser.quit()

    print('\n'.join(output))
    print("\n\n")
    for team, record in team_record.items():
        print(f"{team},{record[0]},{record[1]}")
    for m, record in map_record.items():
        print(f"{m},{record[0]},{record[1]}")


if __name__ == '__main__':
    main()
