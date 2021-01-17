from time import sleep

from selenium import webdriver
from selenium.common.exceptions import NoSuchElementException, StaleElementReferenceException
from selenium.webdriver.common.by import By

USERNAME = "qpwoeirut"
PASSWORD = open("password.txt", "r").read().strip()


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
        for page in range(1, 6):
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
            if cols[2].text in ("Rejected", "Queued", "Pending"):
                continue
            score = cols[3].text
            opp = cols[4].text
            rank = cols[5].text
            linkedTime = time
            links.append("N/A")
            if cols[6].text != "N/A":
                links[-1] = cols[6].find_element(By.CSS_SELECTOR, 'a').get_attribute("href")
                linkedTime = "EQUALSHYPERLINK(\"" + links[-1] + "\"COMMA\"" + time + "\")"

            elems = [linkedTime, opp, score if len(score) > 3 else "N/A", rank]
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

        maps = []
        durations = []
        for i in range(3):
            info = self.browser.find_element(By.CSS_SELECTOR, f"div.gameWrapper > div:nth-of-type({i+1})").text  # 1-indexed
            round_map = info.split('-')[0]
            duration = info.strip().split()[-2]
            maps.append(round_map.strip())
            durations.append(duration.strip())

        return maps, durations


def main():
    # scrims, links = DashboardScraper().run()
    # print(scrims)
    # print(links)
    scrims = ['EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/996cc31b5b502ec87b59962241c51e.bc21"COMMA"1/15/2021 7:59:42 PM"),Chop Suey,1 - 2,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/90fda24a31589cb9232953b1b493c9.bc21"COMMA"1/15/2021 7:50:35 PM"),confused,1 - 2,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/b31bb53bd466d9a5906eb8a7bcf1a3.bc21"COMMA"1/15/2021 9:00:24 PM"),Kryptonite,0 - 3,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/23480c4a401bc1b0da8d974a4fd7f4.bc21"COMMA"1/15/2021 7:41:59 PM"),Propaganda Machine,3 - 0,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/3552e2402993f425f389fb0afe3028.bc21"COMMA"1/15/2021 8:18:18 PM"),monky,1 - 2,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/3879a45ea0e3ee7689cd8a1c665293.bc21"COMMA"1/15/2021 8:36:01 PM"),Muckraker,3 - 0,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/2e508afc097cf8be0696a5fd0c7b87.bc21"COMMA"1/15/2021 8:31:47 PM"),Muckraker,2 - 1,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/599e055798cbbfba27805f1c7bb623.bc21"COMMA"1/15/2021 9:40:32 PM"),Chop Suey,0 - 3,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/8f593653d48bc2f1fc3360976a60df.bc21"COMMA"1/15/2021 9:09:09 PM"),monky,3 - 0,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/1603926631666a2d88922f43619ace.bc21"COMMA"1/15/2021 9:58:36 PM"),confused,3 - 0,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/88dee3c5573671737b782a4265ef74.bc21"COMMA"1/16/2021 8:43:07 AM"),Kryptonite,2 - 1,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/e08d5cd50251c9fae674e8e53b68e4.bc21"COMMA"1/15/2021 10:05:52 PM"),babyducks,0 - 3,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/0ca26c0ce579359952c7c466b9330e.bc21"COMMA"1/15/2021 10:11:08 PM"),Bytecode Mafia,3 - 0,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/aee96e614a8d8ee8b4b32e34e31ad9.bc21"COMMA"1/15/2021 11:44:41 PM"),camel_case,1 - 2,Ranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/909effcee571145c7ae26bcdb0e0a8.bc21"COMMA"1/15/2021 11:34:29 PM"),waffle,3 - 0,Ranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/5efb4f5803218b8e62643a87591035.bc21"COMMA"1/15/2021 11:44:33 PM"),Huge L Club,1 - 2,Ranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/9d0c14a2ae3e6b4296c1339026c8b1.bc21"COMMA"1/15/2021 10:19:48 PM"),Bytecode Mafia,3 - 0,Ranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/7789ba6ecbdcf8d8ee007f2aacb08f.bc21"COMMA"1/16/2021 12:18:49 AM"),AntiVaxxKids,3 - 0,Ranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/e9045ef5fe37dda3b3dc85331a5bad.bc21"COMMA"1/16/2021 12:37:30 AM"),Producing Perfection,0 - 3,Ranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/15c15158a8dda37b4674692a11bca2.bc21"COMMA"1/16/2021 12:06:56 AM"),Bytecode Mafia,2 - 1,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/d94187af840d88a81382fe23cb8482.bc21"COMMA"1/16/2021 7:22:38 AM"),3 Musketeers,1 - 2,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/69952fd76b3b4d361d304a86fa145b.bc21"COMMA"1/16/2021 7:02:54 AM"),confused,2 - 1,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/2858516b8fc00c27cb71cc9f432e58.bc21"COMMA"1/16/2021 5:47:21 AM"),3 Musketeers,0 - 3,Ranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/938b6506647cfdc5d63a1781041c14.bc21"COMMA"1/16/2021 5:33:57 AM"),monky,1 - 2,Ranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/4af93fc02a65203d8406f4569df5f2.bc21"COMMA"1/16/2021 5:41:21 AM"),Huge L Club,3 - 0,Ranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/eb4c758138c676c1ac65fe5fef6b48.bc21"COMMA"1/16/2021 5:25:37 AM"),waffle,3 - 0,Ranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/760aa55b8ef00f32bc23561892b02f.bc21"COMMA"1/16/2021 4:09:30 AM"),I am the Senate,2 - 1,Ranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/1096c8b065c11fc63ccc6e83678322.bc21"COMMA"1/16/2021 6:57:49 AM"),I am the Senate,2 - 1,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/dfe512b225a716d068bd6c00e0cc99.bc21"COMMA"1/16/2021 8:54:38 AM"),monky,1 - 2,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/1711117529fbdb8b556bcd16d0d2d6.bc21"COMMA"1/16/2021 11:52:58 AM"),Huge L Club,2 - 1,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/48a97e1b8f49c7b6e68cdb759adade.bc21"COMMA"1/16/2021 8:53:30 AM"),monky,3 - 0,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/a72b876ab05981618707d75c77261a.bc21"COMMA"1/16/2021 9:23:31 AM"),Chop Suey,0 - 3,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/89e8563215370e5d60d49af8325b82.bc21"COMMA"1/16/2021 11:07:02 AM"),waffle,2 - 1,Ranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/62b4ee806af17bb09edcf97f5be1e5.bc21"COMMA"1/16/2021 11:11:59 AM"),tooOldForThis,1 - 2,Ranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/4fac5b0205d2fbbceff9d7e45d1b36.bc21"COMMA"1/16/2021 11:16:50 AM"),Blue Dragon,3 - 0,Ranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/ba6dd6f6332f58d413ea3778920f0d.bc21"COMMA"1/16/2021 11:30:38 AM"),3 Musketeers,0 - 3,Ranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/94b2a5c53752b9901cc53747344099.bc21"COMMA"1/16/2021 10:48:12 AM"),Rua!!,3 - 0,Ranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/3e77345164178bdc8ea976ffbf6f80.bc21"COMMA"1/16/2021 12:23:48 PM"),Producing Perfection,0 - 3,Ranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/809b5c58f0b7d6e6fe5d9f2c8f9d97.bc21"COMMA"1/16/2021 11:15:14 AM"),Nikola,1 - 2,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/d4b6d9f52da95a033049983e73408d.bc21"COMMA"1/16/2021 11:44:48 AM"),Super Cow Powers,0 - 3,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/e9b88e48e0caa4e9f8f4502c7570fe.bc21"COMMA"1/16/2021 12:01:13 PM"),blair blezers,2 - 1,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/2997d4ae41a248a35e036e53edec3a.bc21"COMMA"1/16/2021 11:47:50 AM"),fishy yum,3 - 0,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/39df1a2225a982024f1bac41f6e2f4.bc21"COMMA"1/16/2021 12:26:36 PM"),The Al Gore Rhythm,2 - 1,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/9c3c71539e0aced939feb54ff5504e.bc21"COMMA"1/16/2021 12:49:10 PM"),fishy yum,3 - 0,Unranked', 'EQUALSHYPERLINK("https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/8e89f7ead165c070644e5cee45f2c1.bc21"COMMA"1/16/2021 1:49:58 PM"),Hard Coders,2 - 1,Unranked']
    links = ['https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/996cc31b5b502ec87b59962241c51e.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/90fda24a31589cb9232953b1b493c9.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/b31bb53bd466d9a5906eb8a7bcf1a3.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/23480c4a401bc1b0da8d974a4fd7f4.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/3552e2402993f425f389fb0afe3028.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/3879a45ea0e3ee7689cd8a1c665293.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/2e508afc097cf8be0696a5fd0c7b87.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/599e055798cbbfba27805f1c7bb623.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/8f593653d48bc2f1fc3360976a60df.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/1603926631666a2d88922f43619ace.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/88dee3c5573671737b782a4265ef74.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/e08d5cd50251c9fae674e8e53b68e4.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/0ca26c0ce579359952c7c466b9330e.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/aee96e614a8d8ee8b4b32e34e31ad9.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/909effcee571145c7ae26bcdb0e0a8.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/5efb4f5803218b8e62643a87591035.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/9d0c14a2ae3e6b4296c1339026c8b1.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/7789ba6ecbdcf8d8ee007f2aacb08f.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/e9045ef5fe37dda3b3dc85331a5bad.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/15c15158a8dda37b4674692a11bca2.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/d94187af840d88a81382fe23cb8482.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/69952fd76b3b4d361d304a86fa145b.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/2858516b8fc00c27cb71cc9f432e58.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/938b6506647cfdc5d63a1781041c14.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/4af93fc02a65203d8406f4569df5f2.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/eb4c758138c676c1ac65fe5fef6b48.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/760aa55b8ef00f32bc23561892b02f.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/1096c8b065c11fc63ccc6e83678322.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/dfe512b225a716d068bd6c00e0cc99.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/1711117529fbdb8b556bcd16d0d2d6.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/48a97e1b8f49c7b6e68cdb759adade.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/a72b876ab05981618707d75c77261a.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/89e8563215370e5d60d49af8325b82.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/62b4ee806af17bb09edcf97f5be1e5.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/4fac5b0205d2fbbceff9d7e45d1b36.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/ba6dd6f6332f58d413ea3778920f0d.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/94b2a5c53752b9901cc53747344099.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/3e77345164178bdc8ea976ffbf6f80.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/809b5c58f0b7d6e6fe5d9f2c8f9d97.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/d4b6d9f52da95a033049983e73408d.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/e9b88e48e0caa4e9f8f4502c7570fe.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/2997d4ae41a248a35e036e53edec3a.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/39df1a2225a982024f1bac41f6e2f4.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/9c3c71539e0aced939feb54ff5504e.bc21', 'https://2021.battlecode.org/visualizer.html?https://2021.battlecode.org/replays/8e89f7ead165c070644e5cee45f2c1.bc21']

    browser = webdriver.Chrome()

    output = []
    for scrim, link in zip(scrims, links):
        maps, durations = MatchScraper(browser, link).run()
        output.append(','.join([scrim, maps[0], durations[0], maps[1], durations[1], maps[2], durations[2]]))

    browser.quit()

    print('\n'.join(output))


if __name__ == '__main__':
    main()
