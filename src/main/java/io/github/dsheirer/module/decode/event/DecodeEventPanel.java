/*
 * ******************************************************************************
 * sdrtrunk
 * Copyright (C) 2014-2018 Dennis Sheirer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * *****************************************************************************
 */
package io.github.dsheirer.module.decode.event;

import io.github.dsheirer.alias.Alias;
import io.github.dsheirer.alias.AliasList;
import io.github.dsheirer.alias.AliasModel;
import io.github.dsheirer.channel.IChannelDescriptor;
import io.github.dsheirer.icon.IconManager;
import io.github.dsheirer.identifier.Identifier;
import io.github.dsheirer.identifier.IdentifierCollection;
import io.github.dsheirer.identifier.Role;
import io.github.dsheirer.module.ProcessingChain;
import io.github.dsheirer.preference.PreferenceType;
import io.github.dsheirer.preference.UserPreferences;
import io.github.dsheirer.sample.Listener;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DecodeEventPanel extends JPanel implements Listener<ProcessingChain>
{
    private final static Logger mLog = LoggerFactory.getLogger(DecodeEventPanel.class);

    private static final long serialVersionUID = 1L;
    private JTable mTable;
    private DecodeEventModel mEmptyDecodeEventModel;
    private JScrollPane mEmptyScroller;
    private IconManager mIconManager;
    private AliasModel mAliasModel;
    private UserPreferences mUserPreferences;
    private PreferenceUpdater mPreferenceUpdater = new PreferenceUpdater();
    private TimestampCellRenderer mTimestampCellRenderer;

    /**
     * View for call event table
     * @param iconManager to display alias icons in table rows
     */
    public DecodeEventPanel(IconManager iconManager, UserPreferences userPreferences, AliasModel aliasModel)
    {
        setLayout(new MigLayout("insets 0 0 0 0", "[grow,fill]", "[grow,fill]"));
        mIconManager = iconManager;
        mAliasModel = aliasModel;
        mUserPreferences = userPreferences;
        mUserPreferences.addPreferenceUpdateListener(mPreferenceUpdater);
        mEmptyDecodeEventModel = new DecodeEventModel();
        mTable = new JTable(mEmptyDecodeEventModel);
        mTable.setAutoCreateRowSorter(true);
        mTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        mTimestampCellRenderer = new TimestampCellRenderer();
        updateCellRenderers();

        mEmptyScroller = new JScrollPane(mTable);
        add(mEmptyScroller);
    }

    private void updateCellRenderers()
    {
        mTable.getColumnModel().getColumn(DecodeEventModel.COLUMN_TIME)
            .setCellRenderer(mTimestampCellRenderer);
        mTable.getColumnModel().getColumn(DecodeEventModel.COLUMN_DURATION)
            .setCellRenderer(new DurationCellRenderer());
        mTable.getColumnModel().getColumn(DecodeEventModel.COLUMN_FROM_ID)
            .setCellRenderer(new IdentifierCellRenderer(Role.FROM));
        mTable.getColumnModel().getColumn(DecodeEventModel.COLUMN_FROM_ALIAS)
            .setCellRenderer(new AliasedIdentifierCellRenderer(Role.FROM));
        mTable.getColumnModel().getColumn(DecodeEventModel.COLUMN_TO_ID)
            .setCellRenderer(new IdentifierCellRenderer(Role.TO));
        mTable.getColumnModel().getColumn(DecodeEventModel.COLUMN_TO_ALIAS)
            .setCellRenderer(new AliasedIdentifierCellRenderer(Role.TO));
        mTable.getColumnModel().getColumn(DecodeEventModel.COLUMN_CHANNEL)
            .setCellRenderer(new ChannelDescriptorCellRenderer());
        mTable.getColumnModel().getColumn(DecodeEventModel.COLUMN_FREQUENCY)
            .setCellRenderer(new FrequencyCellRenderer());
    }

    @Override
    public void receive(ProcessingChain processingChain)
    {
        EventQueue.invokeLater(() -> {
            mTable.setModel(processingChain != null ? processingChain.getDecodeEventModel() : mEmptyDecodeEventModel);

            if(processingChain != null)
            {
                updateCellRenderers();
            }
        });
    }

    /**
     * Custom cell renderer for displaying identifiers from an identifier collection
     */
    public class IdentifierCellRenderer extends DefaultTableCellRenderer
    {
        protected Role mRole;

        /**
         * Constructs an instance of the cell renderer.
         *
         * @param role of the identifier
         */
        public IdentifierCellRenderer(Role role)
        {
            mRole = role;
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if(value instanceof IdentifierCollection)
            {
                List<Identifier> identifiers = ((IdentifierCollection)value).getIdentifiers(mRole);

                if(identifiers.size() > 1)
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Multiple Identifiers:\n");
                    for(Identifier identifier: identifiers)
                    {
                        sb.append("ID:").append(identifier.debug()).append("\n");
                    }
                    mLog.debug(sb.toString());
                }
                label.setText(format(identifiers));
            }
            else
            {
                label.setText(null);
            }

            return label;
        }

        /**
         * Formats a list of identifiers as a comma separated list of values
         * @param identifiers to format
         * @return formatted list or null
         */
        protected String format(List<Identifier> identifiers)
        {
            if(identifiers == null || identifiers.isEmpty())
            {
                return null;
            }

            StringBuilder sb = new StringBuilder();

            for(Identifier identifier: identifiers)
            {
                if(sb.length() > 0)
                {
                    sb.append(",");
                }
                sb.append(mUserPreferences.getTalkgroupFormatPreference().format(identifier));
            }

            return sb.toString();
        }
    }

    /**
     * Cell renderer for identifier aliases
     */
    public class AliasedIdentifierCellRenderer extends DefaultTableCellRenderer
    {
        private Role mRole;

        /**
         * Constructs an instance of the cell renderer.
         *
         * @param role of the identifier
         */
        public AliasedIdentifierCellRenderer(Role role)
        {
            mRole = role;
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            Color color = mTable.getForeground();
            ImageIcon icon = null;
            String text = null;

            if(value instanceof IdentifierCollection)
            {
                IdentifierCollection identifierCollection = (IdentifierCollection)value;
                List<Identifier> identifiers = identifierCollection.getIdentifiers(mRole);

                if(identifiers != null && !identifiers.isEmpty())
                {
                    AliasList aliasList = mAliasModel.getAliasList(identifierCollection);

                    if(aliasList != null)
                    {
                        StringBuilder sb = new StringBuilder();

                        for(Identifier identifier: identifiers)
                        {
                            Alias alias = aliasList.getAlias(identifier);

                            if(alias != null)
                            {
                                if(sb.length() > 0)
                                {
                                    sb.append(",");
                                }
                                sb.append(alias.getName());

                                color = alias.getDisplayColor();
                                icon = mIconManager.getIcon(alias.getIconName(), IconManager.DEFAULT_ICON_SIZE);
                            }
                        }

                        text = sb.toString();
                    }
                }
            }

            label.setText(text);
            label.setForeground(color);
            label.setIcon(icon);

            return label;
        }
    }

    public class TimestampCellRenderer extends DefaultTableCellRenderer
    {
        private SimpleDateFormat mTimestampFormatter;

        public TimestampCellRenderer()
        {
            setHorizontalAlignment(JLabel.CENTER);
            updatePreferences();
        }

        public void updatePreferences()
        {
            mTimestampFormatter = mUserPreferences.getDecodeEventPreference().getTimestampFormat().getFormatter();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if(value instanceof Long)
            {
                label.setText(mTimestampFormatter.format(new Date((long)value)));
            }
            else
            {
                label.setText(null);
            }

            return label;
        }
    }

    public class DurationCellRenderer extends DefaultTableCellRenderer
    {
        private DecimalFormat mDecimalFormat = new DecimalFormat("0.#");
//        private SimpleDateFormat mDurationFormatter = new SimpleDateFormat("s.SSS");

        public DurationCellRenderer()
        {
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String formatted = null;

            if(value instanceof Long)
            {
                long duration = (long)value;

                if(duration > 0)
                {
//                    formatted = mDurationFormatter.format(new Date(duration));
                    formatted = mDecimalFormat.format((double)duration / 1e3d);
                }
            }

            label.setText(formatted);

            return label;
        }
    }

    /**
     * Frequency value cell renderer
     */
    public class FrequencyCellRenderer extends DefaultTableCellRenderer
    {
        private DecimalFormat mFrequencyFormatter = new DecimalFormat("0.00000 MHz");

        public FrequencyCellRenderer()
        {
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String formatted = null;

            if(value instanceof IChannelDescriptor)
            {
                IChannelDescriptor channelDescriptor = (IChannelDescriptor)value;

                long frequency = channelDescriptor.getDownlinkFrequency();

                if(frequency > 0)
                {
                    formatted = mFrequencyFormatter.format(frequency / 1e6d);
                }
            }

            label.setText(formatted);

            return label;
        }
    }

    /**
     * Channel descriptor value cell renderer
     */
    public class ChannelDescriptorCellRenderer extends DefaultTableCellRenderer
    {
        public ChannelDescriptorCellRenderer()
        {
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if(value instanceof IChannelDescriptor)
            {
                label.setText(value.toString());
            }
            else
            {
                label.setText(null);
            }

            return label;
        }
    }

    /**
     * Updates cell renderer(s) when user preferences change.
     */
    public class PreferenceUpdater implements Listener<PreferenceType>
    {
        @Override
        public void receive(PreferenceType preferenceType)
        {
            if(preferenceType == PreferenceType.DECODE_EVENT)
            {
                mTimestampCellRenderer.updatePreferences();
            }
        }
    }
}
