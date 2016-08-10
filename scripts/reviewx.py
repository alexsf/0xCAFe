#! /usr/bin/python

import sys
import json
import urllib
import urllib2
import string

##
## Opinion Extraction
##
opinionExtractUrl = "http://localhost:7171/opinion/review"

##
## Index Server Url
##
##indexServerUrl = "http://192.168.99.100:9200"
indexServerUrl = "http://hack01-vrt.hpeswlab.net:9200"

##
##
##
analyticsUrl = "http://svsedhack162.hpeswlab.net:31880/platform-services/api"


def extract_content(review):
    splitted = review.split("\t")
    splitted.pop(0)
    return ". ".join(splitted)


def filter_non_printable(review):
    printable = set(string.printable)
    return filter(lambda x: x in printable, review)


def process_review(review_json):
    request = urllib2.Request(opinionExtractUrl, review_json, {"X-TENANT-ID": "111800881824924672", "Content-Type": "application/json"})
    response = urllib2.urlopen(request)
    return response.read()

def work(id, review, productid):
    r = extract_content(review)
    data = {}
    r = filter_non_printable(r)
    data["id"] = id
    data["text"] = r
    data["productId"] = productid
    index(data)

    json_data = json.dumps(data)
    submitted_review = process_review(json_data)
    print submitted_review

def is_interesting(review, look_for_features):
    if look_for_features is None:
        return True
    look_for_features_list = look_for_features.split(",")
    for feature in look_for_features_list:
        if feature in review:
            return True
    return False


def index(data):
    payload = {}
    payload["content_primary"] = data["text"]
    elastic_url = indexServerUrl + "/111800881824924672_item/item/" + str(data["id"])
    print elastic_url
    request = urllib2.Request(elastic_url, json.dumps(payload))
    response = urllib2.urlopen(request)
    response.read()

    analytics_url = analyticsUrl + "/sqldata"
    payload = {}
    payload["sql"] = "insert into item(id) values(" + str(data["id"]) + ")"
    request = urllib2.Request(analytics_url, json.dumps(payload), {"X-TENANT-ID": "111800881824924672", "Content-Type": "application/json"})
    response = urllib2.urlopen(request)
    response.read()

def main(argv):
    productid = int(argv[0])
    id = productid
    try:
        look_for_features = argv[1]
    except IndexError:
        look_for_features = None
    for review in sys.stdin:
        try:
            review = review.lower()
            if not is_interesting(review, look_for_features):
                continue
            ## for productid 10000, review ids will be 10001, 10002, 10003, etc
            ## for prodcutid 20000, review ids will be 20001, 20002, 20003
            id += 1
            work(id, review, productid)
        except Exception,e:
            print "Interrupted by user or error occured"
            print str(e)
            break

if __name__ == "__main__":
    main(sys.argv[1:])
