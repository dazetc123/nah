package com.example.myapplication;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class VehicleStatusActivity extends AppCompatActivity {
    private MaterialCardView[] cards;
    private String[] titles;
    private int selected = -1;
    private MaterialButton btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_status);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());

        btnSave = findViewById(R.id.btnSaveStatus);
        titles = new String[]{"Sẵn sàng hoạt động", "Đang bảo trì", "Hỏng – không chạy được"};
        cards = new MaterialCardView[]{
                setup(R.id.optReady, 0, R.drawable.ic_check_circle, R.drawable.bg_icon_green, R.color.colorSuccess,
                        "Xe hoạt động bình thường, có thể nhận chuyến"),
                setup(R.id.optMaintenance, 1, R.drawable.ic_build, R.drawable.bg_icon_amber, R.color.colorWarning,
                        "Đang bảo dưỡng định kỳ hoặc sửa chữa nhỏ"),
                setup(R.id.optBroken, 2, R.drawable.ic_block, R.drawable.bg_icon_red, R.color.colorError,
                        "Xe gặp sự cố, cần hỗ trợ")
        };

        btnSave.setOnClickListener(v -> {
            if (selected < 0) return;
            // TODO: nối API cập nhật trạng thái xe khi backend mở quyền cho tài xế
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Đã cập nhật")
                    .setMessage("Trạng thái xe: " + titles[selected])
                    .setCancelable(false)
                    .setPositiveButton("Xong", (d, w) -> finish())
                    .show();
        });
    }

    private MaterialCardView setup(int includeId, int index, int icon, int iconBg, int tint, String subtitle) {
        MaterialCardView card = findViewById(includeId);
        ImageView iv = card.findViewById(R.id.ivIcon);
        iv.setImageResource(icon);
        iv.setColorFilter(ContextCompat.getColor(this, tint));
        card.findViewById(R.id.iconBg).setBackgroundResource(iconBg);
        ((TextView) card.findViewById(R.id.tvTitle)).setText(titles[index]);
        ((TextView) card.findViewById(R.id.tvSubtitle)).setText(subtitle);
        card.setOnClickListener(v -> select(index));
        return card;
    }

    private void select(int index) {
        selected = index;
        for (int i = 0; i < cards.length; i++) {
            boolean on = i == index;
            cards[i].setChecked(on);
            cards[i].setStrokeColor(ContextCompat.getColor(this, on ? R.color.colorPrimary : R.color.colorCardStroke));
        }
        btnSave.setEnabled(true);
        btnSave.setText("Cập nhật: " + titles[index]);
    }
}
