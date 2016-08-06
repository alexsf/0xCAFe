#! /usr/bin/python

import sys
import json
import urllib
import urllib2
import string

##
## Opinion Extraction
##
opinionExtractUrl = "http://localhost:7171/opinion"

##
## Index Server Url
##
indexServerUrl = "http://192.168.99.100:9200"


def extract_content(review):
    splitted = review.split("\t")
    splitted.pop(0)
    return ". ".join(splitted)


def filter_non_printable(review):
    printable = set(string.printable)
    return filter(lambda x: x in printable, review)


def get_feature_opinion_pairs(review_json):
    review = json.loads(review_json)
    review_text = review["text"]
    request = urllib2.Request(opinionExtractUrl, review_text, {"X-TENANT-ID": "1"})
    response = urllib2.urlopen(request)
    return response.read()

def work(id, review, product):
    r = extract_content(review)
    data = {}
    r = filter_non_printable(r)
    data["id"] = id
    data["text"] = r
    data["product"] = product
    index(data)
    json_data = json.dumps(data)
    fop = get_feature_opinion_pairs(json_data)    
    pairs_obj = json.loads(fop)
    pairs_obj["product"] = product
    print json.dumps(pairs_obj)
    ##pairs = pairs_obj["pairs"]
    ##for pair in pairs:
        ##print pair["feature"] + "\t" + pair["opinion"]


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
    payload["product"] = data["product"]
    payload["text"] = data["text"]
    elastic_url = indexServerUrl + "/1_index/review/" + str(data["id"])
    print elastic_url
    request = urllib2.Request(elastic_url, json.dumps(payload))
    response = urllib2.urlopen(request)
    response.read()

def main(argv):
    id = 0
    product = argv[0]
    try:
        look_for_features = argv[1]
    except IndexError:
        look_for_features = None
    for review in sys.stdin:
        try:
            review = review.lower()
            if not is_interesting(review, look_for_features):
                continue
            id += 1
            work(id, review, product)
        except Exception,e:
            print "Interrupted by user or error occured"
            print str(e)
            break

if __name__ == "__main__":
    main(sys.argv[1:])
