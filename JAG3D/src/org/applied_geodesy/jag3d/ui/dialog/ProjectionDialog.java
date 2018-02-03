package org.applied_geodesy.jag3d.ui.dialog;

import java.sql.SQLException;
import java.util.Optional;

import org.applied_geodesy.adjustment.network.observation.projection.Projection;
import org.applied_geodesy.adjustment.network.observation.projection.ProjectionType;
import org.applied_geodesy.jag3d.sql.SQLManager;
import org.applied_geodesy.jag3d.ui.table.CellValueType;
import org.applied_geodesy.jag3d.ui.textfield.DoubleTextField;
import org.applied_geodesy.jag3d.ui.textfield.DoubleTextField.ValueSupport;
import org.applied_geodesy.util.i18.I18N;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.util.Callback;

public class ProjectionDialog {

	private static I18N i18n = I18N.getInstance();
	private static ProjectionDialog projectionDialog = new ProjectionDialog();
	private Dialog<Projection> dialog = null;
	private Window window;
	private DoubleTextField heightTextField;
	private CheckBox directionReductionCheckBox;
	private CheckBox heightReductionCheckBox;
	private RadioButton gaussKruegerReductionRadioButton;
	private RadioButton utmReductionRadioButton;

	private ProjectionDialog() {}

	public static void setOwner(Window owner) {
		projectionDialog.window = owner;
	}

	public static Optional<Projection> showAndWait() {
		projectionDialog.init();
		projectionDialog.load();
		return projectionDialog.dialog.showAndWait();
	}


	private void init() {
		if (this.dialog != null)
			return;

		this.dialog = new Dialog<Projection>();

		this.dialog.setTitle(i18n.getString("ProjectionDialog.title", "Horizontal projection"));
		this.dialog.setHeaderText(i18n.getString("ProjectionDialog.header", "Horizontal projection properties"));
		this.dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		this.dialog.initModality(Modality.APPLICATION_MODAL);
		//		this.dialog.initStyle(StageStyle.UTILITY);
		this.dialog.initOwner(window);

		this.dialog.getDialogPane().setContent(this.createPane());
		this.dialog.setResizable(true);

		this.dialog.setResultConverter(new Callback<ButtonType, Projection>() {
			@Override
			public Projection call(ButtonType buttonType) {
				if (buttonType == ButtonType.OK) {
					ProjectionType type = getProjectionType();
					double referenceHeight = heightTextField.getNumber();

					Projection projection = new Projection(type);
					projection.setReferenceHeight(referenceHeight);

					save(projection);

				}
				return null;
			}
		});
	}

	private Node createPane() {
		String directionLabel   = i18n.getString("ProjectionDialog.reduction.direction.title", "Direction reduction");
		String directionTooltip = i18n.getString("ProjectionDialog.reduction.direction.tooltip", "If checked, direction reduction will be applied during network adjustment");

		String heightLabel      = i18n.getString("ProjectionDialog.reduction.height.title", "Height reduction");
		String heightTooltip    = i18n.getString("ProjectionDialog.reduction.height.tooltip", "If checked, height reduction of horizontal distances will be applied during network adjustment");

		String gkLabel          = i18n.getString("ProjectionDialog.reduction.gk.title", "Gau\u00df-Kr\u00fcger reduction");
		String gkTooltip        = i18n.getString("ProjectionDialog.reduction.gk.tooltip", "If checked, Gau\u00df-Kr\u00fcger reduction of horizontal distances will be applied during network adjustment");

		String utmLabel         = i18n.getString("ProjectionDialog.reduction.gk.title", "UTM reduction");
		String utmTooltip       = i18n.getString("ProjectionDialog.reduction.gk.tooltip", "If checked, UTM reduction of horizontal distances will be applied during network adjustment");

		this.heightTextField = new DoubleTextField(0.0, CellValueType.LENGTH, true, ValueSupport.NON_NULL_VALUE_SUPPORT);
		this.heightTextField.setTooltip(new Tooltip(i18n.getString("ProjectionDialog.reduction.height.value.tooltip", "Height w.r.t. survey datum")));
		this.heightTextField.setMinWidth(100);
		this.heightTextField.setPrefWidth(150);
		this.heightTextField.setMaxWidth(Double.MAX_VALUE);
		
		this.directionReductionCheckBox = new CheckBox();
		this.directionReductionCheckBox.setGraphic(new Label(directionLabel));
		this.directionReductionCheckBox.setTooltip(new Tooltip(directionTooltip));
		this.directionReductionCheckBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.directionReductionCheckBox.setMaxWidth(Double.MAX_VALUE);
		

		this.heightReductionCheckBox = new CheckBox();
		this.heightReductionCheckBox.setGraphic(new Label(heightLabel));
		this.heightReductionCheckBox.setTooltip(new Tooltip(heightTooltip));
		this.heightReductionCheckBox.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.heightReductionCheckBox.setMaxWidth(Double.MAX_VALUE);
		

		this.gaussKruegerReductionRadioButton = new RadioButton();
		this.gaussKruegerReductionRadioButton.setGraphic(new Label(gkLabel));
		this.gaussKruegerReductionRadioButton.setTooltip(new Tooltip(gkTooltip));
		this.gaussKruegerReductionRadioButton.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.gaussKruegerReductionRadioButton.setMaxWidth(Double.MAX_VALUE);

		
		this.utmReductionRadioButton = new RadioButton();
		this.utmReductionRadioButton.setGraphic(new Label(utmLabel));
		this.utmReductionRadioButton.setTooltip(new Tooltip(utmTooltip));
		this.utmReductionRadioButton.setMinSize(Control.USE_PREF_SIZE, Control.USE_PREF_SIZE);
		this.utmReductionRadioButton.setMaxWidth(Double.MAX_VALUE);

		this.gaussKruegerReductionRadioButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue)
					utmReductionRadioButton.setSelected(false);
			}
		});

		this.utmReductionRadioButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue)
					gaussKruegerReductionRadioButton.setSelected(false);
			}
		});

		ToggleButton gaussKruegerReductionToggleButton = new ToggleButton();
		ToggleButton utmReductionToggleButton = new ToggleButton();

		gaussKruegerReductionToggleButton.setGraphic(this.gaussKruegerReductionRadioButton);
		utmReductionToggleButton.setGraphic(this.utmReductionRadioButton);

		gaussKruegerReductionToggleButton.selectedProperty().bindBidirectional(this.gaussKruegerReductionRadioButton.selectedProperty());
		utmReductionToggleButton.selectedProperty().bindBidirectional(this.utmReductionRadioButton.selectedProperty());

		ToggleGroup group = new ToggleGroup();
		group.getToggles().addAll(gaussKruegerReductionToggleButton, utmReductionToggleButton);

		GridPane gridPane = new GridPane();
		gridPane.setMaxWidth(Double.MAX_VALUE);
		gridPane.setHgap(20);
		gridPane.setVgap(7);
		gridPane.setPadding(new Insets(5, 10, 5, 10)); // oben, recht, unten, links

		GridPane.setHgrow(this.directionReductionCheckBox, Priority.SOMETIMES);
		GridPane.setHgrow(this.heightReductionCheckBox, Priority.SOMETIMES);
		GridPane.setHgrow(this.heightTextField, Priority.ALWAYS);
		GridPane.setHgrow(this.gaussKruegerReductionRadioButton, Priority.SOMETIMES);
		GridPane.setHgrow(this.utmReductionRadioButton, Priority.SOMETIMES);	
		
		int row = 0;
		gridPane.add(this.directionReductionCheckBox,       0, ++row, 2, 1);
		gridPane.add(this.heightReductionCheckBox,          0, ++row, 1, 1);
		gridPane.add(this.heightTextField,                  1,   row, 1, 1);

		gridPane.add(this.gaussKruegerReductionRadioButton, 0, ++row, 2, 1);
		gridPane.add(this.utmReductionRadioButton,          0, ++row, 2, 1);
		
		Platform.runLater(new Runnable() {
			@Override public void run() {
				directionReductionCheckBox.requestFocus();
			}
		});

		return gridPane;
	}

	private ProjectionType getProjectionType() {
		boolean directionReduction = this.directionReductionCheckBox.isSelected();
		boolean heightReduction    = this.heightReductionCheckBox.isSelected();
		boolean gkReduction        = this.gaussKruegerReductionRadioButton.isSelected();
		boolean utmReduction       = this.utmReductionRadioButton.isSelected();

		if (directionReduction && heightReduction && gkReduction)
			return ProjectionType.DIRECTION_HEIGHT_GK_REDUCTION;

		else if (directionReduction && heightReduction && utmReduction)
			return ProjectionType.DIRECTION_HEIGHT_UTM_REDUCTION;

		else if (directionReduction && heightReduction)
			return ProjectionType.DIRECTION_HEIGHT_REDUCTION;

		else if (directionReduction && gkReduction)
			return ProjectionType.DIRECTION_GK_REDUCTION;

		else if (directionReduction && utmReduction)
			return ProjectionType.DIRECTION_UTM_REDUCTION;

		else if (heightReduction && gkReduction)
			return ProjectionType.HEIGHT_GK_REDUCTION;

		else if (heightReduction && utmReduction)
			return ProjectionType.HEIGHT_UTM_REDUCTION;

		else if (directionReduction)
			return ProjectionType.DIRECTION_REDUCTION;

		else if (heightReduction)
			return ProjectionType.HEIGHT_REDUCTION;

		else if (gkReduction)
			return ProjectionType.GAUSS_KRUEGER_REDUCTION;

		else if (utmReduction)
			return ProjectionType.UTM_REDUCTION;

		return ProjectionType.NONE;
	}

	private void load() {
		try {
			Projection projection = SQLManager.getInstance().getProjectionDefinition();
			this.heightTextField.setNumber(projection.getReferenceHeight());

			this.directionReductionCheckBox.setSelected(projection.isDirectionReduction());
			this.heightReductionCheckBox.setSelected(projection.isHeightReduction());
			this.gaussKruegerReductionRadioButton.setSelected(projection.isGaussKruegerReduction());
			this.utmReductionRadioButton.setSelected(projection.isUTMReduction());
		}
		catch (SQLException e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("ProjectionDialog.message.error.sql.load.title", "SQL-Error"),
							i18n.getString("ProjectionDialog.message.error.sql.load.header", "Error, could not load data."),
							i18n.getString("ProjectionDialog.message.error.sql.load.message", "An exception occure during saving dataset to database."),
							e
							);
				}
			});
		}
	}

	private void save(Projection projection) {
		try {
			SQLManager.getInstance().save(projection);
		}
		catch (SQLException e) {
			e.printStackTrace();
			Platform.runLater(new Runnable() {
				@Override public void run() {
					OptionDialog.showThrowableDialog (
							i18n.getString("ProjectionDialog.message.error.sql.save.title", "SQL-Error"),
							i18n.getString("ProjectionDialog.message.error.sql.save.header", "Error, could not save changes in database table"),
							i18n.getString("ProjectionDialog.message.error.sql.save.message", "An exception occure during saving dataset to database."),
							e
							);
				}
			});
		}
	}
}
