# wk-shell-decomposition-Cytoscape
Weighted k-shell decomposition Cytoscape plugins
wk-shell-decomposition is a Cytoscape 3 app that performs a k-shell decomposition on a network using a weighted algorithm. k-shell decomposition is a method that ranks the most important nodes in a network and partitions them into shells based on that rank. This algorithm assigns a weight based on the degree of a node and the adjacent nodes. This app will generate a column _wkshell on the network's node table that stores the rank and partitions the network into ordinal k-shells. The app then adjusts the layout of the network to packed concentric rings sorted by k-shell and applies a gradient to the color of the nodes by k-shell. This app can be run by accessing it under the apps menu or by calling the command:

```javascript
wkshell decompose
```

## Download
You can download this app through the app store by accessing the store through the Cytoscape client and searching for "**wk-shell-decomposition**". You can also download it at the app page: http://apps.cytoscape.org/apps/wkshelldecomposition
