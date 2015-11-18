from __future__ import print_function
import httplib2
import urllib2
import requests
import json
import os

from apiclient import errors
from apiclient import discovery
import oauth2client
from oauth2client import client
from oauth2client import tools

try:
    import argparse
    flags = argparse.ArgumentParser(parents=[tools.argparser]).parse_args()
except ImportError:
    flags = None

#SCOPES = 'https://www.googleapis.com/auth/gmail.readonly'
SCOPES = 'https://www.googleapis.com/auth/gmail.modify'
CLIENT_SECRET_FILE = 'client_secret_inmind.json'
APPLICATION_NAME = 'py_email'

def HeaderMap(hlst):
    hmap = {}
    for h in hlst:
        hmap[h['name']] = h['value']
    return hmap

def get_credentials():
    """Gets valid user credentials from storage.

    If nothing has been stored, or if the stored credentials are invalid,
    the OAuth2 flow is completed to obtain the new credentials.

    Returns:
        Credentials, the obtained credential.
    """
    home_dir = os.path.expanduser('~')
    credential_dir = os.path.join(home_dir, '.credentials')
    if not os.path.exists(credential_dir):
        os.makedirs(credential_dir)
    credential_path = os.path.join(credential_dir,
                                   'gmail-python-quickstart.json')

    store = oauth2client.file.Storage(credential_path)
    credentials = store.get()
    if not credentials or credentials.invalid:
        flow = client.flow_from_clientsecrets(CLIENT_SECRET_FILE, SCOPES)
        flow.user_agent = APPLICATION_NAME
        if flags:
            credentials = tools.run_flow(flow, store, flags)
        else: # Needed only for compatability with Python 2.6
            credentials = tools.run(flow, store)
        print('Storing credentials to ' + credential_path)
    return credentials

def SummarizeFrom():
    credentials = get_credentials()
    http = credentials.authorize(httplib2.Http())
    service = discovery.build('gmail', 'v1', http=http)

    response = service.users().messages().list(userId = 'me', labelIds = 'UNREAD').execute()
    messages = []
    if 'messages' in response:
        messages.extend(response['messages'])
    msgnum = min(len(messages),5)
    senderlst = []
    for idx in range(msgnum):
        msg_id = messages[idx]['id']
        msg = service.users().messages().get(userId = 'me', id = msg_id).execute()
        headerMap = HeaderMap(msg['payload']['headers'])
        if CheckMeeting(msg['snippet'],headerMap['From'],headerMap['Subject']):
            senderlst.append('Schedule')
        else:senderlst.append(headerMap['From'].split('<')[0])
    return senderlst

def CheckMeeting(body,sender,subject):
    try:
        url = 'http://birch.speech.cs.cmu.edu:5000/distract'
        data = {'Body':body,'Sender':sender,'Subject':subject}
        req = urllib2.Request('http://birch.speech.cs.cmu.edu:5000/distract')
        req.add_header('Content-type', 'application/json')
        resp = urllib2.urlopen(req, json.dumps(data))
        resp_json = json.loads(resp.read())
        if resp_json['Type']==1: return True
        else: return False
    except urllib2.HTTPError, err:
        return False
    
def GetMsgLst():
    credentials = get_credentials()
    http = credentials.authorize(httplib2.Http())
    service = discovery.build('gmail', 'v1', http=http)

    response = service.users().messages().list(userId = 'me', labelIds = 'UNREAD').execute()
    messages = []
    if 'messages' in response:
        messages.extend(response['messages'])
    return messages

def ReadMsgFrom(sender):
    credentials = get_credentials()
    http = credentials.authorize(httplib2.Http())
    service = discovery.build('gmail', 'v1', http=http)
        
    messages = GetMsgLst()
    msgnum = min(len(messages),5)
    for idx in range(msgnum):
        msg_id = messages[idx]['id']
        msg = service.users().messages().get(userId = 'me', id = msg_id).execute()
        headerMap = HeaderMap(msg['payload']['headers'])
        if sender.lower()==headerMap['From'].split('<')[0].split()[0].lower():
            return ReadMsgById(msg_id)
    return 'no messages from '+sender

def ReadSchduleEmail():
    credentials = get_credentials()
    http = credentials.authorize(httplib2.Http())
    service = discovery.build('gmail', 'v1', http=http)
    
    messages = GetMsgLst()
    msgnum = min(len(messages),5)
    for idx in range(msgnum):
        msg_id = messages[idx]['id']
        msg = service.users().messages().get(userId = 'me', id = msg_id).execute()
        headerMap = HeaderMap(msg['payload']['headers'])
        #if sender.lower()==headerMap['From'].split('<')[0].split()[0].lower():
            #return ReadMsgById(msg_id)
        content = ExtractMeeting(msg['snippet'],headerMap['From'],headerMap['Subject'])
        if len(content)>2: return content
    return 'no messages about scheduling'

def ExtractMeeting(body,sender,subject):
    try:
        url = 'http://birch.speech.cs.cmu.edu:5000/distract'
        data = {'Body':body,'Sender':sender,'Subject':subject}
        req = urllib2.Request('http://birch.speech.cs.cmu.edu:5000/distract')
        req.add_header('Content-type', 'application/json')
        resp = urllib2.urlopen(req, json.dumps(data))
        resp_json = json.loads(resp.read())
        if resp_json['Type']==1:
            respStr = 'Meeting scheduling from '+resp_json['who'].split()[0]+', it is about '+resp_json['what']+', at '+resp_json['When']
	    return respStr
        else: return ''
    except urllib2.HTTPError, err:
        return ''
    

def CheckInBox():
    credentials = get_credentials()
    http = credentials.authorize(httplib2.Http())
    service = discovery.build('gmail', 'v1', http=http)

    response = service.users().messages().list(userId = 'me', labelIds = 'UNREAD').execute()
    messages = []
    if 'messages' in response:
        messages.extend(response['messages'])
    print(messages[0])
    return len(messages)

def ReadMsgById(msg_id):
    credentials = get_credentials()
    http = credentials.authorize(httplib2.Http())
    service = discovery.build('gmail', 'v1', http=http)
    
    msg = service.users().messages().get(userId = 'me', id = msg_id).execute()
    headerMap = HeaderMap(msg['payload']['headers'])
    sender = headerMap['From'].split('<')[0]
    return sender+' said, '+msg['snippet']

#check if "the first email" is urgent
#if yes, report it and mark it as read
def CheckUrgent(msg_id):
    credentials = get_credentials()
    http = credentials.authorize(httplib2.Http())
    service = discovery.build('gmail', 'v1', http=http)

    msg = service.users().messages().get(userId = 'me', id = msg_id).execute()
    if 'urgent' in msg['snippet']:
        headerMap = HeaderMap(msg['payload']['headers'])
        sender = headerMap['From'].split('<')[0]
        ModifyMessage(service=service, user_id='me', msg_id=msg_id, msg_labels=MsgLabelSetRead())
        return sender+' said, '+msg['snippet']
    else: return ''


def ModifyMessage(service, user_id, msg_id, msg_labels):
    try:
        message = service.users().messages().modify(userId=user_id, id=msg_id,
		    body=msg_labels).execute()
        label_ids = message['labelIds']
	#print 'Message ID: %s - With Label IDs %s' % (msg_id, label_ids)
        return message
    except errors.HttpError, error:
	#print 'An error occured '
	print('An error occurred')


def MsgLabelSetRead():
    return {'removeLabelIds': ['UNREAD'], 'addLabelIds': []}

def main():
    """Shows basic usage of the Gmail API.

    Creates a Gmail API service object and outputs a list of label names
    of the user's Gmail account.
    """
    credentials = get_credentials()
    http = credentials.authorize(httplib2.Http())
    service = discovery.build('gmail', 'v1', http=http)

    results = service.users().labels().list(userId='me').execute()
    labels = results.get('labels', [])

    if not labels:
        print('No labels found.')
    else:
      print('Labels:')
      for label in labels:
        print(label['name'])

def msg_main():
    credentials = get_credentials()
    http = credentials.authorize(httplib2.Http())
    service = discovery.build('gmail', 'v1', http=http)

    response = service.users().messages().list(userId = 'me', labelIds = 'UNREAD').execute()
    messages = []
    if 'messages' in response:
        messages.extend(response['messages'])
    msg_id = messages[0]['id']
    print(messages[0])

    msg = service.users().messages().get(userId = 'me', id = msg_id).execute()
    headerMap = HeaderMap(msg['payload']['headers'])
    print(headerMap['From'])
    print(headerMap['Subject'])
    print(msg['snippet'])
    url = 'http://birch.speech.cs.cmu.edu:5000/distract'
    #data = {'Body':'please reply my previous email as soon as possible','Sender':headerMap['From'],'Subject':headerMap['Subject']}
    data = {'Body':'Hello, everyone, we will meet for our project at 13:00, Nov 1. It seems to work for all of us.','Sender':headerMap['From'],'Subject':headerMap['Subject']}
    req = urllib2.Request('http://birch.speech.cs.cmu.edu:5000/distract')
    req.add_header('Content-type', 'application/json')
    response = urllib2.urlopen(req, json.dumps(data))
    """
    headers = {'content-type': 'application/json'}
    response = requests.post(url, data=json.dumps(data), headers=headers)
    """
    #print(response.read())
    respJson = json.loads(response.read())
    print(respJson)
    

if __name__ == '__main__':
    msg_main()
    #num_of_msg = CheckInBox()
    #print(str(num_of_msg))
    #SummarizeFrom()
