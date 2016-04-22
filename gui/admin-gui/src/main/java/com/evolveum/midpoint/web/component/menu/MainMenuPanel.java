/*
 * Copyright (c) 2010-2016 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.midpoint.web.component.menu;

import com.evolveum.midpoint.gui.api.component.BasePanel;
import com.evolveum.midpoint.gui.api.page.PageBase;
import com.evolveum.midpoint.web.component.breadcrumbs.Breadcrumb;
import com.evolveum.midpoint.web.component.breadcrumbs.BreadcrumbPageClass;
import com.evolveum.midpoint.web.component.util.VisibleEnableBehaviour;
import com.evolveum.midpoint.web.security.SecurityUtils;
import com.evolveum.midpoint.web.session.SessionStorage;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;

import java.io.Serializable;
import java.util.List;

/**
 * @author Viliam Repan (lazyman)
 */
public class MainMenuPanel extends BasePanel<MainMenuItem> {
	private static final long serialVersionUID = 1L;

    private static final String ID_ITEM = "item";
    private static final String ID_LINK = "link";
    private static final String ID_LABEL = "label";
    private static final String ID_ICON = "icon";
    private static final String ID_SUBMENU = "submenu";
    private static final String ID_ARROW = "arrow";
    private static final String ID_BUBBLE = "bubble";
    private static final String ID_SUB_ITEM = "subItem";
    private static final String ID_SUB_LINK = "subLink";
    private static final String ID_SUB_LABEL = "subLabel";

    public MainMenuPanel(String id, IModel<MainMenuItem> model) {
        super(id, model);
        initLayout();
    }

    private void initLayout() {
        final MainMenuItem menu = getModelObject();

        WebMarkupContainer item = new WebMarkupContainer(ID_ITEM);
        item.add(AttributeModifier.replace("class", new AbstractReadOnlyModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
            public String getObject() {
                if (menu.isMenuActive((WebPage) getPage())) {
                    return "active";
                }

                for (MenuItem item : menu.getItems()) {
                    if (item.isMenuActive((WebPage) getPage())) {
                        return "active";
                    }
                }

                return !menu.getItems().isEmpty() ? "treeview" : null;
            }
        }));
        add(item);

        WebMarkupContainer link;
        if (menu.getPage() != null) {
            link = new AjaxLink(ID_LINK) {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    mainMenuPerformed(menu);
                }
            };
        } else {
            link = new WebMarkupContainer(ID_LINK);
        }
        item.add(link);

        WebMarkupContainer icon = new WebMarkupContainer(ID_ICON);
        icon.add(AttributeModifier.replace("class", new PropertyModel<>(menu, MainMenuItem.F_ICON_CLASS)));
        link.add(icon);

        Label label = new Label(ID_LABEL, menu.getName());
        link.add(label);
        
        final PropertyModel<String> bubbleModel = new PropertyModel<>(menu, MainMenuItem.F_BUBBLE_LABEL);
                
        Label bubble = new Label(ID_BUBBLE, bubbleModel);
        bubble.add(new VisibleEnableBehaviour() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return bubbleModel.getObject() != null;
			}
        });
        link.add(bubble);

        WebMarkupContainer arrow = new WebMarkupContainer(ID_ARROW);
        arrow.add(new VisibleEnableBehaviour() {
			private static final long serialVersionUID = 1L;
		
			@Override
		    public boolean isVisible() {
		        return !menu.getItems().isEmpty() && bubbleModel.getObject() == null;
		    }
		});
        link.add(arrow);
        
        WebMarkupContainer submenu = new WebMarkupContainer(ID_SUBMENU);
        submenu.add(new VisibleEnableBehaviour() {
			private static final long serialVersionUID = 1L;
		
			@Override
		    public boolean isVisible() {
		        return !menu.getItems().isEmpty();
		    }
		});
        item.add(submenu);

        ListView<MenuItem> subItem = new ListView<MenuItem>(ID_SUB_ITEM, new Model((Serializable) menu.getItems())) {

            @Override
            protected void populateItem(ListItem<MenuItem> listItem) {
                createSubmenu(listItem);
            }
        };
        submenu.add(subItem);
    }

    private void createSubmenu(final ListItem<MenuItem> listItem) {
        final MenuItem menu = listItem.getModelObject();

        listItem.add(AttributeModifier.replace("class", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return menu.isMenuActive((WebPage) getPage()) ? "active" : null;
            }
        }));

        Link subLink = new Link(ID_SUB_LINK) {


            @Override
            public void onClick() {
                menuItemPerformed(menu);
            }
        };
        listItem.add(subLink);

        Label subLabel = new Label(ID_SUB_LABEL, menu.getName());
        subLink.add(subLabel);

        listItem.add(new VisibleEnableBehaviour() {

            @Override
            public boolean isVisible() {
                MenuItem mi = listItem.getModelObject();

                boolean visible = true;
                if (mi.getVisibleEnable() != null) {
                    visible = mi.getVisibleEnable().isVisible();
                }

                return visible && SecurityUtils.isMenuAuthorized(mi);
            }

            @Override
            public boolean isEnabled() {
                MenuItem mi = listItem.getModelObject();

                if (mi.getVisibleEnable() == null) {
                    return true;
                }

                return mi.getVisibleEnable().isEnabled();
            }
        });
    }

    private void menuItemPerformed(MenuItem menu) {
        SessionStorage storage = getPageBase().getSessionStorage();
        storage.clearBreadcrumbs();

        MainMenuItem mainMenuItem = getModelObject();
        Breadcrumb bc = new Breadcrumb(mainMenuItem.getName());
        bc.setIcon(new Model<>(mainMenuItem.getIconClass()));
        storage.pushBreadcrumb(bc);

        List<MenuItem> items = mainMenuItem.getItems();
        if (!items.isEmpty()) {
            MenuItem first = items.get(0);

            BreadcrumbPageClass invisibleBc = new BreadcrumbPageClass(first.getName(), first.getPage(),
                    first.getParams());
            invisibleBc.setVisible(false);
            storage.pushBreadcrumb(invisibleBc);
        }

        setResponsePage(menu.getPage(), menu.getParams());
    }

    private void mainMenuPerformed(MainMenuItem menu) {
        SessionStorage storage = getPageBase().getSessionStorage();
        storage.clearBreadcrumbs();

        if (menu.getParams() == null) {
            setResponsePage(menu.getPage());
        } else {
            setResponsePage(menu.getPage(), menu.getParams());
        }
    }
}
