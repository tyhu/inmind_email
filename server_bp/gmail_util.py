from __future__ import print_function
import httplib2
import os

from apiclient import discovery
import oauth2client
from oauth2client import client
from oauth2client import tools

try:
    import argparse
    flags = argparse.ArgumentParser(parents=[tools.argparser]).parse_args()
except ImportError:
    flags = None

SCOPES = 'https://www.googleapis.com/auth/gmail.readonly'
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
        senderlst.append(headerMap['From'].split('<')[0])
    return senderlst
    
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
    print(msg['snippet'])
    

if __name__ == '__main__':
    msg_main()
    #num_of_msg = CheckInBox()
    #print(str(num_of_msg))
    #SummarizeFrom()
