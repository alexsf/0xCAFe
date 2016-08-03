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

def work(review):
    r = extract_content(review)
    data = {}
    r = filter_non_printable(r)
    data["text"] = r
    json_data = json.dumps(data)
    fop = get_feature_opinion_pairs(json_data)    
    pairs_obj = json.loads(fop)
    #print fop
    pairs = pairs_obj["pairs"]
    for pair in pairs:
        print pair["feature"] + "\t" + pair["opinion"]


def main():
    for review in sys.stdin:
        try:
            work(review.lower())
        except:
            print "Interrupted by user or error occured"
            break

main()
