import csv
from parsel import Selector
from selenium import webdriver
from selenium.webdriver.common.keys import Keys

import pandas as pd
writer = csv.writer(open('testing.csv', 'w')) # preparing csv file to store parsing result later
writer.writerow(['name', 'job_title', 'schools', 'location', 'ln_url'])
driver = webdriver.Firefox(executable_path='D:\\geckodriver.exe')
driver.get('https://www.linkedin.com/')

driver.find_element_by_xpath('//a[text()="Sign in"]').click()

username_input = driver.find_element_by_name('session_key')
username_input.send_keys('dhanunayak.ram@gmail.com')

password_input = driver.find_element_by_name('session_password')
password_input.send_keys('Amruthap@1')

# click on the sign in button
# we're finding Sign in text button as it seems this element is seldom to be changed
driver.find_element_by_xpath('//button[text()="Sign in"]').click()

DF= pd.read_csv("D://linkedin.csv")
df=DF.drop(columns=["Unnamed: 0"])
df=df['Url']

# visit each profile in linkedin and grab detail we want to get
for profile in df:
    driver.get(profile)

    try:
        sel = Selector(text=driver.page_source)
        name = sel.xpath('//title/text()').extract_first().split(' | ')[0]
        job_title = sel.xpath('//h2/text()').extract_first().strip()
        schools = ', '.join(sel.xpath('//*[contains(@class, "pv-entity__school-name")]/text()').extract())
        location = sel.xpath('//*[@class="t-16 t-black t-normal inline-block"]/text()').extract_first().strip()
        ln_url = driver.current_url
        """
        you can add another logic in case parsing is failed, ie because no job title is found
        because the linkedin user isn't add it
        """
    except:
        print('failed')

    # print to console for testing purpose
   

    writer.writerow([name, job_title, schools, location, ln_url])

driver.quit()