package org.demo;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.*;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.ui.*;

import java.util.*;

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

        ttable.setChildrenAllowed(3, false);
        ttable.setChildrenAllowed(4, false);

        /*ttable.setChildrenAllowed(rootId, true);
        ttable.setChildrenAllowed(1, true);
        ttable.setChildrenAllowed(2, true);*/

        ttable.setCollapsed(rootId, false);
        ttable.setCollapsed(1, false);
        ttable.setCollapsed(2, false);

        ttable.setDragMode(Table.TableDragMode.ROW);

        List<Integer> goodIds = new ArrayList<>();
        goodIds.add(1);
        goodIds.add(2);
        Object[] ids = goodIds.toArray();

        AcceptCriterion criterion = new And(new AbstractSelect.TargetItemIs(ttable, ids),
                new AbstractSelect.AcceptItem(ttable, ids));


        ttable.setDropHandler(new DropHandler() {
            @Override
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


                if (ttable.hasChildren(sourceItemId) && (!sourceItemId.toString().equals(rootId))) {
                    // container.setParent(sourceItemId, rootId);
                    //targetItemId = container.getParent(targetItemId);

                    // find the direction of movement
                    int sourceIndex = -1;
                    int targetIndex = -1;
                    int count = 0;
                    for (Object id : ttable.getItemIds()) {
                        if (id.equals(sourceItemId)) {
                            sourceIndex = count;
                        }

                        if (id.equals(targetItemId)) {
                            targetIndex = count;
                        }
                        count++;
                    }

                    if (sourceIndex < targetIndex) {
                        container.moveAfterSibling(sourceItemId, targetItemId);
                    } else {
                        container.moveAfterSibling(targetItemId, sourceItemId);
                    }
                }
            }

            @Override
            public AcceptCriterion getAcceptCriterion() {
                return criterion;
            }
        });
        return ttable;
    }


    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
