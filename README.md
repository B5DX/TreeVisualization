# TreeVisualization
Caculate the coordinate of the leaves of a tree, and show it with JAVA Swing.

急于交工导致软件结构十分混乱，实在没法看，所以只把计算节点位置的核心算法展示以供参考。

Node类表示节点，每个节点在GUI上表示为一个长方形，高为定值，宽根据所含字符长度确定。
由节点所在层数可以方便的确定y坐标（横轴为x轴），重点是计算x方向的位置。
为节点添加矩形框参数：即lLength, rLength。现解释如下：考虑一个节点（下称节点N）连同其所有孩子所构成的整个子树，节点N（一个长方形）的中点x坐标到他的子树中最左侧的孩子节点的左边框x坐标的距离为lLength，同理有rLength为节点N（一个长方形）的中点x坐标到他的子树中最右侧的孩子节点的右边框x坐标的距离。有了这两个参数便可以计算每个节点的x坐标，具体成员变量如下

class Node {
	int x, y; //节点的坐标位置
	
	int h; //节点在树中的深度 
	
	int lLength, rLength; //节点左、右矩形框参数，用于计算坐标位置 
	
	Node parent; //父节点指针 
	
	Vector<Node> children; //子节点指针向量 
	
}

算法思路如下：
树结构调整遵循几个基本原则：
1.根节点位置不变，x轴上位于窗口中间。
2.x轴上，父节点的中点位于极左和极右两个孩子中点连线的中点（只有一个孩子则中点对齐）。
3.节点的同级节点矩形框不交叠。
4.y方向坐标只与深度有关。
为树节点增加矩形框参数，利用树节点位置坐标计算算法从根节点出发设置所有节点的位置，能够很 好地自动调节树的结构。当节点发生改变后，从变化结点开始沿父节点回溯到根节点，更新沿途的节点矩形框参数。然后保证根节点位置不动，从根节点向下根据矩形框参数设置所有节点的位置。设置树结构调节规则：x 方向上，1.父节点位置为子节点极左节点边框中心和极右节点边框中心位置 的中点。2.根节点位置不变；y 方向上，坐标只与深度有关。 对于每个节点，建立矩形框的数学模型：每个节点都有一个矩形框，将该节点及其所有子节点包含在 框内。叶子结点矩形框根据字符串长度确定，长度较小则设为默认。我们只关心 x 方向的矩形框位置，y 方向可设置只与层数有关。每个矩形框包含两个参数：lLength 和 rLength，代表左边框距离根节点的长度 和右边框距离根节点的长度。有了这两个值加上上述规则，就可以通过我们的算法保证根节点位置不动来 美观合理的安排所有节点的位置。具体算法如下： 第一步，根据变化的节点（如新插入的、删除的节点或者更新了内容的节点）向上回溯更新沿途所有 父节点矩形框参数：当一个节点的矩形框参数变化后，父节点矩形框可能变化，非父亲节点则一定不变。 计算方法为：父节点 lLength 等于子节点极左与极右中心长度（width）的一半加上极左节点 lLength；父节 点 rLength 等于 width 的一半加上极右节点 rLength。向上递归可以更新所有矩形框。 第二部：由已知的根节点坐标，向下层次遍历同时设置所有节点的位置。父节点坐标确定后，第一个 子节点 x 坐标为父节点 x 减去 width/2,。其余子节点可顺序根据前一个子节点位置加上前一节点 rLength 加 上当前节点 lLength 计算得出，从而设置好该层所有节点位置。向下递归可以设置所有节点位置。