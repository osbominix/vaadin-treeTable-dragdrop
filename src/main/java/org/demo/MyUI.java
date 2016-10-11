package org.demo;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.ui.*;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
public class MyUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout layout = new VerticalLayout();

        TreeTable ttable = generateTreeTable();


        layout.addComponents(ttable);
        layout.setMargin(true);
        layout.setSpacing(true);
        
        setContent(layout);
    }

    private TreeTable generateTreeTable() {
        TreeTable ttable = new TreeTable("My TreeTable");
        ttable.addContainerProperty("Name", String.class, "");
        ttable.setWidth("20em");

        int rootId = 0;

// Create the tree nodes
        ttable.addItem(new Object[]{"Root"}, rootId);
        ttable.addItem(new Object[]{"Branch 1"}, 1);
        ttable.addItem(new Object[]{"Branch 2"}, 2);
        ttable.addItem(new Object[]{"Leaf 1"}, 3);
        ttable.addItem(new Object[]{"Leaf 2"}, 4);
        ttable.addItem(new Object[]{"Leaf 3"}, 5);
        ttable.addItem(new Object[]{"Leaf 4"}, 6);

// Set the hierarchy
        ttable.setParent(1, rootId);
        ttable.setParent(2, rootId);
        ttable.setParent(3, 1);
        ttable.setParent(4, 1);
        ttable.setParent(5, 2);
        ttable.setParent(6, 2);

        ttable.setCollapsed(rootId, false);
        ttable.setCollapsed(1, false);
        ttable.setCollapsed(2, false);

        ttable.setDragMode(Table.TableDragMode.ROW);

        ttable.setDropHandler(new DropHandler() {


            public AcceptCriterion getAcceptCriterion() {

                return AcceptAll.get();

            }

            public void drop(DragAndDropEvent event) {
                DataBoundTransferable t = (DataBoundTransferable)
                        event.getTransferable();

                AbstractSelect.AbstractSelectTargetDetails target =
                        (AbstractSelect.AbstractSelectTargetDetails) event.getTargetDetails();

                Object sourceItemId = t.getData("itemId");
                Object targetItemId = target.getItemIdOver();

                for (Object itemId = targetItemId; itemId != null; itemId = ttable.getParent(itemId)) {
                    if (itemId == sourceItemId) {
                        return;
                    }
                }

                VerticalDropLocation location = target.getDropLocation();
                HierarchicalContainer container =
                        (HierarchicalContainer) ttable.getContainerDataSource();


                if (ttable.hasChildren(sourceItemId)) {
                    //treeTable.hasChildren(targetItemId)
                    if (location == VerticalDropLocation.MIDDLE) {
//                        System.out.println("Drag to Middle");
                    } else if (location == VerticalDropLocation.TOP) {
                        if (!ttable.hasChildren(targetItemId)) {
                            return;
                        }
                        if ((rootId - (int)targetItemId) != 0) {
                            Object parentId = container.getParent(targetItemId);
                            container.setParent(sourceItemId, parentId);
                            container.moveAfterSibling(targetItemId, sourceItemId);
                        }

                    } else if (location == VerticalDropLocation.BOTTOM) {
                        if (ttable.hasChildren(targetItemId)) {
                            return;
                        }

                        Object targetParentId = container.getParent(targetItemId);
                        Object parentId = container.getParent(targetParentId);
                        container.setParent(sourceItemId, parentId);
                        container.moveAfterSibling(sourceItemId, targetParentId);


                    }
                }
            }
        });
        return ttable;
    }


    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
