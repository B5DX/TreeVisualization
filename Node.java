import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

import java.util.*;

public class Node {
    static JPanel panel;//树节点所在
    static PaintPanel paintPanel;//连接线所在
    //	static Graphics graphics;
    static int defaultLabelLength = 28;
    static int defaultLabelHeight = 28;
    static int rootX, rootY;
    static Node root;
    static int yGap = 60;//纵向填充
    static int xGap = 5;//横向填充
    static RoundBorder blackBorder = new RoundBorder(Color.BLACK);
    static RoundBorder redBorder = new RoundBorder(Color.RED);
    static RoundBorder bluBorder = new RoundBorder(new Color(146, 210, 255));
    static boolean selectable = true;
    static MouseHandle mouseHandle;
    static CodeExhibit codeExhibit;

    static JLabel focusLabel;

    int x, y;
    int h;//深度
    int lLength, rLength;
    JLabel label;
    Node parent;
    Vector<Node> children = new Vector<>();

    public Node() {
    }

    public Node(JPanel p, int x0, int y0, MouseHandle mHandle, CodeExhibit cExhibit) {//根节点构造方法,此前要让framesetVisible
        rootX = x = x0;
        rootY = y = y0;
        h = 1;
        panel = p;
        panel.setLayout(null);
//		paintPanel = paintP;
//		graphics = paintPanel.getGraphics();
        lLength = rLength = defaultLabelLength;
        label = new JLabel("Root", JLabel.CENTER);
        label.setBorder(blackBorder);
        label.setBounds(x0 - defaultLabelLength, y0, defaultLabelLength * 2, defaultLabelHeight);
        p.add(label);
        parent = null;
        root = this;
        mouseHandle = mHandle;
        codeExhibit = cExhibit;
        label.addMouseListener(mouseHandle);
    }

    private Node(Node p, String text) {//普通节点构造方法
        x = y = -1;//还没有计算坐标
        h = p.h + 1;
        label = new JLabel(text, JLabel.CENTER);
        label.setBorder(blackBorder);
        parent = p;
        freshLabelSize(text);
        p.children.add(this);
        panel.add(label);
        label.addMouseListener(mouseHandle);
    }

    void setPaintPanel(PaintPanel p) {
        paintPanel = p;
    }

    void setFocus(JLabel label) {
        focusLabel = label;
    }

    // 设置Node对应的JLabel的边框，用于表示选中
    public void setLabelBorder(Border border) {
        label.setBorder(border);
    }

    public Node findNode(Node cur, JLabel targetLabel) {//从cur节点开始寻找其子树
        if (cur.label == targetLabel) return cur;
        Vector<Node> V = new Vector<>();
        V.add(cur);
        while (V.size() != 0) {
            cur = V.firstElement();
            V.remove(0);
            for (Node i : cur.children) {
                if (i.label == targetLabel)
                    return i;
                else {
                    V.add(i);
                }
            }
        }
        return null;
    }

    public Node findNode(Node cur, String text) {//用于查找按键
        if (cur.label.getText().equals(text)) return cur;
        Vector<Node> V = new Vector<Node>();
        V.add(cur);
        while (V.size() != 0) {
            cur = V.firstElement();
            V.remove(0);
            for (Node i : cur.children) {
                if (i.label.getText().equals(text))
                    return i;
                else {
                    V.add(i);
                }
            }
        }
        return null;
    }

    private void freshLabelSize(String text) {
        FontMetrics metrics = label.getFontMetrics(label.getFont());
        int len = metrics.stringWidth(text) / 2 + 5; //字符串的宽
        if (len <= defaultLabelLength)
            lLength = rLength = defaultLabelLength + xGap;
        else
            lLength = rLength = len + xGap;
        label.setSize((lLength - xGap) * 2, defaultLabelHeight);
        label.setText(text);
    }

    protected void freshAll(Node changedNode) {//以changedNode为起点向上回溯更新矩形参数(changedNode也会修改)
        freshRect(changedNode);
        freshLocation(root);//重置位置
        paintPanel.repaint();//重绘连线
    }

    protected void addSubNode(String text) {//在父节点p下添加子节点
        Node sub = new Node(this, text);
        freshAll(this);
    }

    public void addNode(JLabel targetLabel, String text) {//根据选中的Label添加子节点，需要进行遍历查找
        Node targetNode = findNode(root, targetLabel);//从根节点,找到相应Node
        if (targetNode == null) return;
        else {
            targetNode.addSubNode(text);
        }
//		if(targetNode.label == targetLabel) {
//			cur.addSubNode(text);
//			return;
//		}
//		for(int i=0; i<cur.children.size(); i++) {
//			addNode(cur.children.elementAt(i), targetLabel, text);
//		}
    }

    private void hideLabel(Node cur) {//隐藏cur及其子节点
        cur.label.setVisible(false);
        for (Node i : cur.children) {
            hideLabel(i);
        }
    }

    public void deleteNode(JLabel targetLabel) {//删除整个子树（包含根节点，即targetLabel所在节点）
        Node targetNode = findNode(root, targetLabel);
        hideLabel(targetNode);
        targetNode.children.clear();
        Node p = targetNode.parent;
        if (p != null) {
            for (int i = 0; i < p.children.size(); i++) {
                if (p.children.elementAt(i) == targetNode) {
                    p.children.remove(i);
                    break;
                }
            }
        } else {
            targetLabel.setVisible(true);
            targetLabel.setBorder(blackBorder);
        }
        System.gc();
        freshAll(p);
        focusLabel = null;
    }

    int countWidth(Vector<Node> chd) {
        int count = 0;//计算chd节点列表极左和极右节点位置之差
        if (chd.size() > 1) {//只有一个孩子不执行,因为无需计算count
            for (int i = 1; i < chd.size(); i++) {
                count += chd.elementAt(i - 1).rLength + chd.elementAt(i).lLength;
            }
        }//完成count计算
        else {//小于等于1个孩子，width=0
            count = 0;
        }
        return count;
    }

    //刷新cur节点的矩形参数,然后向上回溯
    public void freshRect(Node cur) {
        if (cur == null)
            return;

        //非空节点
        if (cur.children.size() != 0) {
            Vector<Node> chd = cur.children;
            int width = countWidth(chd);
            int targetLLength = chd.firstElement().lLength + width / 2;
            int targetRLength = chd.lastElement().rLength + width / 2;
            if (cur.lLength < targetLLength) cur.lLength = targetLLength;
            if (cur.rLength < targetRLength) cur.rLength = targetRLength;
        }
        freshRect(cur.parent);
    }

    public void freshLocation(Node p) {//根据p,刷新p的子节点的位置，从root开始,向下遍历
        if (p == null) return;
        if (p == root) {
            p.setLocation(rootX);
        }
        int size = p.children.size();//孩子数目
        if (size == 0) return;//没有孩子直接返回
        if (size == 1) {
            p.children.elementAt(0).setLocation(p.x);
            freshLocation(p.children.elementAt(0));
            return;
        }
        //大于等于两个孩子
        int width = countWidth(p.children);
        p.children.elementAt(0).setLocation(p.x - width / 2);
        for (int i = 1; i < size; i++) {
            Node last = p.children.elementAt(i - 1);
            Node cur = p.children.elementAt(i);
            cur.setLocation(last.x + last.rLength + cur.lLength);
        }
        for (int i = 0; i < size; i++)
            freshLocation(p.children.elementAt(i));
    }

    void setLocation(int x) {//设置cur节点的label位置
        label.setLocation(x - label.getWidth() / 2, rootY + (h - 1) * yGap);
        this.x = x;
    }

    public void drawLink(Graphics graphics, Node cur) {//绘制cur节点与其孩子的连线，从root开始递归绘出全部
        Vector<Node> chd = cur.children;
        if (chd.size() == 0) return;//没有孩子不向下画线
        int parentCenterX = cur.label.getX() + cur.label.getWidth() / 2;
        int parentCenterY = cur.label.getY() + cur.label.getHeight() - 1;
        int centerLineY = (int) (parentCenterY + (yGap - defaultLabelHeight) * 0.4);//中间分支水平线的Y
        graphics.drawLine(parentCenterX, parentCenterY, parentCenterX, centerLineY);

//		System.out.println("centerY:"+centerLineY+", Y:"+parentCenterY);
        JLabel firstLabel = chd.firstElement().label;
        JLabel lastLabel = chd.lastElement().label;
        graphics.drawLine(firstLabel.getX() + firstLabel.getWidth() / 2, centerLineY,
                lastLabel.getX() + lastLabel.getWidth() / 2, centerLineY);
        for (int i = 0; i < chd.size(); i++) {
            JLabel target = chd.elementAt(i).label;
            int X = target.getX() + target.getWidth() / 2;
            int Y = target.getY();
            graphics.drawLine(X, centerLineY, X, Y);
            drawLink(graphics, chd.elementAt(i));
        }
    }

    public void updateData(JLabel targetLabel, String newText) {//更新按钮
        Node target = findNode(root, targetLabel);
        target.freshLabelSize(newText);
        freshAll(target);
    }

    public Node findNode(String text) {//查找按钮
        return findNode(root, text);
    }

    public static void main(String[] args) {
//		Vector<Integer> vector = new Vector<Integer>();
//		vector.add(1);
//		vector.add(2);
//		vector.remove(0);
//		for(Integer integer : vector) {
//			System.out.println(vector.elementAt(0));
//		}
//		System.out.println("s");
    }
}