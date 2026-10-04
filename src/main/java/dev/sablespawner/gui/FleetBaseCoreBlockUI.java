package dev.sablespawner.gui;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Switch;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.utils.TagBuilder;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.sablespawner.blockentity.FleetBaseCoreBlockEntity;
import dev.sablespawner.config.FleetBaseCoreConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class FleetBaseCoreBlockUI {

    private static final ResourceLocation STYLE = StylesheetManager.MC;

    public static ModularUI build(BlockUIMenuType.BlockUIHolder holder) {
        if ( !(holder.player.level().getBlockEntity(holder.pos) instanceof FleetBaseCoreBlockEntity core) ) {
            return ModularUI.of(UI.of(new Label().setText(
                    Component.translatable("sablespawner.gui.missing"))), holder.player);
        }

        UIElement root = new UIElement();

        root.addClass("panel_bg");
        root.layout(l -> l.flexDirection(FlexDirection.COLUMN).paddingAll(8).gapAll(6).width(170) );
        root.addChildren(
                buildTitle(core),
                buildRadiusController(core),
                buildEnableSwitch(core),
                buildForceLoadSwitch(core)
        );

        return ModularUI.of(UI.of(root,
                StylesheetManager.INSTANCE.getStylesheetSafe(STYLE)), holder.player);
    }
    public static UIElement buildTitle(FleetBaseCoreBlockEntity core) {
        UIElement root = new UIElement();
        root.layout(l -> l.flexDirection(FlexDirection.ROW).gapAll(6) );

        Label label = new Label();
        label.setText(Component.translatable("container.sablespawner.fleet_base_core"));
        label.layout(l -> l.flex(1));

        Button infoButton = new Button();
        infoButton.setText(Component.literal("i"));
        infoButton.layout(l -> l.width(10).height(10));
        infoButton.addEventListener(UIEvents.HOVER_TOOLTIPS, event ->
                event.hoverTooltips = new HoverTooltips(infoLines(core), null, null, null));

        return root.addChildren(
                label,
                infoButton
        );
    }
    public static UIElement buildRadiusController(FleetBaseCoreBlockEntity core) {
        UIElement root = new UIElement();
        root.layout(l -> l.flexDirection(FlexDirection.COLUMN).gapAll(6) );

        return root.addChildren(
                buildRadiusTextField(core),
                buildRadiusStepButton(core)
        );
    }
        public static UIElement buildRadiusTextField(FleetBaseCoreBlockEntity core) {
            UIElement root = new UIElement();
            root.layout(l -> l.flexDirection(FlexDirection.ROW).gapAll(4).alignItems(AlignItems.CENTER));

            Label label = new Label();
            label.setText(Component.translatable("sablespawner.gui.fleet_base_core.radius"));
            label.textStyle(style -> style.adaptiveWidth(true));

            TextField textField = new TextField();
            textField.setNumbersOnlyInt(
                            FleetBaseCoreConfig.MIN_RADIUS.getAsInt(),
                            FleetBaseCoreConfig.MAX_RADIUS.getAsInt())
                    .setText( String.valueOf(core.getRadius()) );

            textField.bind(DataBindingBuilder.string(
                    () -> String.valueOf(core.getRadius()),
                    raw -> {
                        if ( raw.isEmpty() ) { return; }
                        try {
                            int value = Integer.parseInt(raw);
                            if ( value < FleetBaseCoreConfig.MIN_RADIUS.getAsInt()
                                    || value > FleetBaseCoreConfig.MAX_RADIUS.getAsInt() ) { return; }
                            core.setRadius(value);
                        } catch ( NumberFormatException ignored ) { }
                    }
            ).build());

            textField.textFieldStyle(style ->
                            style.placeholder(Component.translatable("sablespawner.gui.fleet_base_core.radius_hint"))
                    )
                    .layout(l -> l.flex(1));

            return root.addChildren(label, textField);
        }
        public static UIElement buildRadiusStepButton(FleetBaseCoreBlockEntity core) {
        UIElement root = new UIElement().layout(l -> l.flexDirection(FlexDirection.ROW).gapAll(4).alignItems(AlignItems.CENTER));

        Button stepDown = new Button();
        stepDown.setText("-")
                .setOnClick(e ->
                        e.currentElement.sendMessage("radius_step", TagBuilder.compound().add("delta", -currentStep()).build()))
                .onMessage("radius_step", payload ->
                        core.setRadius(core.getRadius() + payload.getInt("delta")));
        stepDown.layout(l -> l.flex(1));

        Button stepUp = new Button();
        stepUp.setText("+")
                .setOnClick(e ->
                        e.currentElement.sendMessage("radius_step", TagBuilder.compound().add("delta", currentStep()).build()))
                .onMessage("radius_step", payload ->
                        core.setRadius(core.getRadius() + payload.getInt("delta")));
        stepUp.layout(l -> l.flex(1));

        root.addEventListener(UIEvents.TICK, event -> {
            if ( !LDLib2.isClient() ) { return; }
            stepDown.setText(Component.literal("-" + currentStep()));
            stepUp.setText(Component.literal("+" + currentStep()));
        });

        return root.addChildren(stepDown, stepUp);
    }
    public static UIElement buildEnableSwitch(FleetBaseCoreBlockEntity core) {
        UIElement root = new UIElement();
        root.layout(l -> l.flexDirection(FlexDirection.ROW).gapAll(4).alignItems(AlignItems.CENTER));

        Label label = new Label();
        label.setText(Component.translatable("sablespawner.gui.fleet_base_core.protect_switch"));
        label.layout(l -> l.flex(1));

        Switch switch1 = new Switch();
        switch1.bind(DataBindingBuilder.bool(core::isEnabled, core::setEnabled).build());

        return root.addChildren(label, switch1);
    }
    public static UIElement buildForceLoadSwitch(FleetBaseCoreBlockEntity core) {
        UIElement root = new UIElement();
        root.layout(l -> l.flexDirection(FlexDirection.ROW).gapAll(4).alignItems(AlignItems.CENTER));

        Label label = new Label();
        label.setText(Component.translatable("sablespawner.gui.fleet_base_core.force_load_switch"));
        label.layout(l -> l.flex(1));

        Switch switch1 = new Switch();
        switch1.bind(DataBindingBuilder.bool(core::isForceLoaded, core::setForceLoaded).build());

        return root.addChildren(label, switch1);
    }

    private static int currentStep() {
        if ( UIElement.isCtrlDown() )  { return FleetBaseCoreConfig.RADIUS_STEP_FINE.getAsInt(); }
        if ( UIElement.isShiftDown() ) { return FleetBaseCoreConfig.RADIUS_STEP_COARSE.getAsInt(); }
        return FleetBaseCoreConfig.RADIUS_STEP.getAsInt();
    }
    private static List<Component> infoLines(FleetBaseCoreBlockEntity core) {
        ObjectList<Component> lines = new ObjectArrayList<>();

        lines.add(Component.translatable("sablespawner.gui.fleet_base_core.info.core_short_id",
                core.getCoreId().toString().substring(0, 8)));
        lines.add(Component.translatable("sablespawner.gui.fleet_base_core.info.core_id",
                core.getCoreId().toString()));
        lines.add(Component.translatable("sablespawner.gui.fleet_base_core.info.owner",
                core.getOwnerName().isEmpty()
                        ? Component.translatable("sablespawner.gui.fleet_base_core.info.owner.none")
                        : Component.literal(core.getOwnerName())));
        lines.add(Component.translatable("sablespawner.gui.fleet_base_core.info.bound",
                Component.translatable(core.isBoundToSubLevel() ? "gui.yes" : "gui.no")));

        return lines;
    }
}
