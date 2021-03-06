/*
 * Copyright (c) 2017, Kronos <https://github.com/KronosDesign>
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.devtools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Collection;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import static net.runelite.api.widgets.WidgetInfo.TO_CHILD;
import static net.runelite.api.widgets.WidgetInfo.TO_GROUP;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.PluginPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevToolsPanel extends PluginPanel
{
	private static final Logger logger = LoggerFactory.getLogger(DevToolsPanel.class);

	private final EmptyBorder PADDING_BORDER = new EmptyBorder(3, 3, 3, 3);

	private JButton renderPlayersBtn = new JButton();
	private JButton renderNpcsBtn = new JButton();
	private JButton renderGroundItemsBtn = new JButton();
	private JButton renderGroundObjectsBtn = new JButton();
	private JButton renderGameObjectsBtn = new JButton();
	private JButton renderWallsBtn = new JButton();
	private JButton renderDecorBtn = new JButton();
	private JButton renderInventoryBtn = new JButton();
	private JButton settingsSnapshotBtn = new JButton();
	private JButton settingsClearBtn = new JButton();

	private JLabel textLbl = new JLabel();
	private JLabel textColorLbl = new JLabel();
	private JLabel nameLbl = new JLabel();
	private JLabel modelLbl = new JLabel();
	private JLabel textureLbl = new JLabel();
	private JLabel typeLbl = new JLabel();
	private JLabel contentTypeLbl = new JLabel();

	private final Client client;
	private final DevToolsPlugin plugin;

	private final SettingsTracker settingsTracker;

	@Inject
	public DevToolsPanel(@Nullable Client client, DevToolsPlugin plugin)
	{
		this.client = client;
		this.plugin = plugin;

		settingsTracker = new SettingsTracker(client);

		setMinimumSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
		setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
		setSize(PANEL_WIDTH, PANEL_HEIGHT);
		setLayout(new BorderLayout());
		setVisible(true);

		add(createOptionsPanel(), BorderLayout.NORTH);
		add(createWidgetTreePanel(), BorderLayout.CENTER);
	}

	private JPanel createOptionsPanel()
	{
		JPanel container = new JPanel();
		container.setLayout(new GridLayout(5, 2, 3, 3));
		container.setBorder(PADDING_BORDER);

		renderPlayersBtn = new JButton("Players");
		renderPlayersBtn.addActionListener(e ->
		{
			highlightButton(renderPlayersBtn);
			plugin.togglePlayers();
		});
		container.add(renderPlayersBtn);

		renderNpcsBtn = new JButton("NPCs");
		renderNpcsBtn.addActionListener(e ->
		{
			highlightButton(renderNpcsBtn);
			plugin.toggleNpcs();
		});
		container.add(renderNpcsBtn);

		renderGroundItemsBtn = new JButton("Ground Items");
		renderGroundItemsBtn.addActionListener(e ->
		{
			highlightButton(renderGroundItemsBtn);
			plugin.toggleGroundItems();
		});
		container.add(renderGroundItemsBtn);

		renderGroundObjectsBtn = new JButton("Ground Objects");
		renderGroundObjectsBtn.addActionListener(e ->
		{
			highlightButton(renderGroundObjectsBtn);
			plugin.toggleGroundObjects();
		});
		container.add(renderGroundObjectsBtn);

		renderGameObjectsBtn = new JButton("Game Objects");
		renderGameObjectsBtn.addActionListener(e ->
		{
			highlightButton(renderGameObjectsBtn);
			plugin.toggleGameObjects();
		});
		container.add(renderGameObjectsBtn);

		renderWallsBtn = new JButton("Walls");
		renderWallsBtn.addActionListener(e ->
		{
			highlightButton(renderWallsBtn);
			plugin.toggleWalls();
		});
		container.add(renderWallsBtn);

		renderDecorBtn = new JButton("Decorations");
		renderDecorBtn.addActionListener(e ->
		{
			highlightButton(renderDecorBtn);
			plugin.toggleDecor();
		});
		container.add(renderDecorBtn);

		renderInventoryBtn = new JButton("Inventory");
		renderInventoryBtn.addActionListener(e ->
		{
			highlightButton(renderInventoryBtn);
			plugin.toggleInventory();
		});
		container.add(renderInventoryBtn);

		settingsSnapshotBtn = new JButton("Get Settings");
		settingsSnapshotBtn.addActionListener(settingsTracker::snapshot);
		container.add(settingsSnapshotBtn);

		settingsClearBtn = new JButton("Clear Settings");
		settingsClearBtn.addActionListener(settingsTracker::clear);
		container.add(settingsClearBtn);

		return container;
	}

	private JPanel createWidgetTreePanel()
	{
		JPanel container = new JPanel();
		container.setLayout(new BorderLayout());

		JTree tree = new JTree(new DefaultMutableTreeNode());
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.getSelectionModel().addTreeSelectionListener(e ->
		{
			Object selected = tree.getLastSelectedPathComponent();
			if (selected instanceof WidgetTreeNode)
			{
				WidgetTreeNode node = (WidgetTreeNode) selected;
				Widget widget = node.getWidget();
				plugin.currentWidget = widget;
				plugin.itemIndex = widget.getItemId();
				setWidgetInfo(widget);
				logger.debug("Set widget to {} and item index to {}", widget, widget.getItemId());
			}
			else if (selected instanceof WidgetItemNode)
			{
				WidgetItemNode node = (WidgetItemNode) selected;
				plugin.itemIndex = node.getWidgetItem().getIndex();
				logger.debug("Set item index to {}", plugin.itemIndex);
			}
		});

		JScrollPane scrollPane = new JScrollPane(tree);
		scrollPane.setBorder(PADDING_BORDER);
		container.add(scrollPane, BorderLayout.CENTER);

		JButton refreshWidgetsBtn = new JButton("Refresh Widgets");
		refreshWidgetsBtn.addActionListener(e ->
		{
			DefaultMutableTreeNode root = refreshWidgets();
			tree.setModel(new DefaultTreeModel(root));
		});

		JPanel btnContainer = new JPanel();
		btnContainer.setLayout(new BorderLayout());
		btnContainer.setBorder(PADDING_BORDER);
		btnContainer.add(refreshWidgetsBtn);
		container.add(btnContainer, BorderLayout.NORTH);

		JPanel infoContainer = new JPanel();
		infoContainer.setLayout(new GridLayout(0, 1));

		textLbl = new JLabel("Text: ");
		textColorLbl = new JLabel("Text Color: ");
		nameLbl = new JLabel("Name: ");
		modelLbl = new JLabel("Model ID: ");
		textureLbl = new JLabel("Texture ID: ");
		typeLbl = new JLabel("Type: ");
		contentTypeLbl = new JLabel("Content Type: ");

		infoContainer.add(textLbl);
		infoContainer.add(textColorLbl);
		infoContainer.add(nameLbl);
		infoContainer.add(modelLbl);
		infoContainer.add(textureLbl);
		infoContainer.add(typeLbl);
		infoContainer.add(contentTypeLbl);

		JScrollPane infoScrollPane = new JScrollPane(infoContainer);
		infoScrollPane.setBorder(new EmptyBorder(6, 6, 6, 6));
		container.add(infoScrollPane, BorderLayout.SOUTH);

		return container;
	}

	private void setWidgetInfo(Widget widget)
	{
		if (widget == null)
		{
			return;
		}

		textLbl.setText("Text: " + widget.getText().trim());
		textColorLbl.setText("Text Color: " + widget.getTextColor());
		nameLbl.setText("Name: " + widget.getName().trim());
		modelLbl.setText("Model ID: " + widget.getModelId());
		textureLbl.setText("Sprite ID: " + widget.getSpriteId());
		typeLbl.setText("Type: " + widget.getType()
			+ " Parent " + (widget.getParentId() == -1 ? -1 : TO_GROUP(widget.getParentId()) + "." + TO_CHILD(widget.getParentId())));
		contentTypeLbl.setText("Content Type: " + widget.getContentType() + " Hidden " + widget.isHidden());
	}

	private void highlightButton(JButton button)
	{
		if (button.getBackground().equals(Color.GREEN))
		{
			button.setBackground(null);
		}
		else
		{
			button.setBackground(Color.GREEN);
		}
	}

	private DefaultMutableTreeNode refreshWidgets()
	{
		Widget[] rootWidgets = client.getWidgetRoots();
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();

		plugin.currentWidget = null;
		plugin.itemIndex = -1;

		for (Widget widget : rootWidgets)
		{
			DefaultMutableTreeNode childNode = addWidget("R", widget);
			if (childNode != null)
			{
				root.add(childNode);
			}
		}

		return root;
	}

	private DefaultMutableTreeNode addWidget(String type, Widget widget)
	{
		if (widget == null || widget.isHidden())
		{
			return null;
		}

		DefaultMutableTreeNode node = new WidgetTreeNode(type, widget);

		Widget[] childComponents = widget.getDynamicChildren();
		if (childComponents != null)
		{
			for (Widget component : childComponents)
			{
				DefaultMutableTreeNode childNode = addWidget("D", component);
				if (childNode != null)
				{
					node.add(childNode);
				}
			}
		}

		childComponents = widget.getStaticChildren();
		if (childComponents != null)
		{
			for (Widget component : childComponents)
			{
				DefaultMutableTreeNode childNode = addWidget("S", component);
				if (childNode != null)
				{
					node.add(childNode);
				}
			}
		}

		childComponents = widget.getNestedChildren();
		if (childComponents != null)
		{
			for (Widget component : childComponents)
			{
				DefaultMutableTreeNode childNode = addWidget("N", component);
				if (childNode != null)
				{
					node.add(childNode);
				}
			}
		}

		Collection<WidgetItem> items = widget.getWidgetItems();
		if (items != null)
		{
			for (WidgetItem item : items)
			{
				if (item == null)
				{
					continue;
				}

				node.add(new WidgetItemNode(item));
			}
		}

		return node;
	}

}
