import requests

#URL = 'http://128.237.136.19:9000'
#URL = 'http://localhost:9000'
URL = 'http://192.168.0.6:9000'

r = requests.get(URL)
print r
print r.headers
print r.text

payload = {'Command': 'check-in-box'}
r = requests.post(URL, data=payload)
print r
print r.headers
print r.text
