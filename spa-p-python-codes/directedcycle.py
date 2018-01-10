"""
source -- https://algocoding.wordpress.com/2015/04/02/detecting-cycles-in-a-directed-graph-with-dfs-python/
"""

def cycle_exists(G):  # - G is a directed graph
    color = {u: "white" for u in G}  # - All nodes are initially white
    found_cycle = [False]  # - Define found_cycle as a list so we can change
    # its value per reference, see:
    # http://stackoverflow.com/questions/11222440/python-variable-reference-assignment
    for u in G:  # - Visit all nodes.
        if color[u] == "white":
            dfs_visit(G, u, color, found_cycle)
        if found_cycle[0]:
            break
    return found_cycle[0]


# -------

def dfs_visit(G, u, color, found_cycle):
    if found_cycle[0]:  # - Stop dfs if cycle is found.
        return
    color[u] = "gray"  # - Gray nodes are in the current path
    for v in G[u]:  # - Check neighbors, where G[u] is the adjacency list of u.
        if color[v] == "gray":  # - Case where a loop in the current path is present.
            found_cycle[0] = True
            return
        if color[v] == "white":  # - Call dfs_visit recursively.
            dfs_visit(G, v, color, found_cycle)
    color[u] = "black"  # - Mark node as done.

#print(cycle_exists({'s1':['s3'], 's2':['s1'], 's3':[]}))