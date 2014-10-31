#Popularity Score

The purpose of this feature is to give users the most relevant items when they browse. All of the browse results meet the requirements of the filters, but they should not be rendered equal. Popular items would appear at the top, unless otherwise specified.

##Algorithm

The idea is that we infer how popular an items is by giving it a popularity score, depending on how users interact with the item. Each type of interaction is assigned a different weight.

###Types of Interactions
1. Add an item to favorite list
1. Access detail page via search
1. View details of a page
1. Hover on an ingredient or function
1. Add an item to dislike list

(More to come)

###Stages
Three possible implementations:

####Naive
Only implements interaction #2, #3, and #4. Does not personalize. Every user sees results in the same order.

####Personalized
In addition to the naive approach, take account into interaction #1 and #5.

####Advanced
Take time into account. Recent interactions weight more than old interactions.


###Special case

####Discontinued

We don't want to change the popularity score of discontinued products. They should either disappear from the browse results or stay at the very bottom, because browse is for users to discover and potentially buy products. Seeing lots of discontinued products is frustrating. Quoting Christina, one of our users:

>Additionally, is there a reason for the high percentage of search results featuring discontinued products? It seems kind of frustrating if someone's looking for their perfect product and finds out most of them are no longer available. Could they be moved lower on the list so they're not so prominent? It might be kind of disheartening otherwise. My search for sunscreens lacking alcohol and avobenzone turned up a ton of discontinued results, and I kind of want to go cry in a hole somewhere.
