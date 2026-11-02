package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.trip.ChuyenDanhSach;
import com.example.myapplication.utils.TrangThaiChuyenUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {

    public interface OnTripClick {
        void onClick(ChuyenDanhSach chuyen);
    }

    private final List<ChuyenDanhSach> items = new ArrayList<>();
    private final OnTripClick listener;

    public TripAdapter(OnTripClick listener) {
        this.listener = listener;
    }

    public void submit(List<ChuyenDanhSach> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChuyenDanhSach c = items.get(position);
        holder.tvMaChuyen.setText(String.format(Locale.getDefault(), "Chuyến #%d", c.getIdChuyen()));
        holder.tvTenCongTrinh.setText(c.getTenCongTrinh() == null ? "Công trình" : c.getTenCongTrinh());
        holder.tvBienSo.setText(c.getBienSo());
        holder.tvKhoiLuong.setText(c.getKhoiLuong() == null ? "" :
                String.format(Locale.getDefault(), "%.1f m³", c.getKhoiLuong()));
        holder.tvThoiGianGiao.setText(c.getThoiGianGiao() == null ? "Chưa có lịch giao"
                : "Giao dự kiến: " + TrangThaiChuyenUtil.dinhDangNgayGio(c.getThoiGianGiao()));
        holder.tvTrangThai.setText(c.getTenTrangThai());
        TrangThaiChuyenUtil.toMauTrangThai(holder.tvTrangThai, c.getTrangThai());
        holder.itemView.setOnClickListener(v -> listener.onClick(c));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMaChuyen, tvTenCongTrinh, tvBienSo, tvKhoiLuong, tvThoiGianGiao, tvTrangThai;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMaChuyen = itemView.findViewById(R.id.tvMaChuyen);
            tvTenCongTrinh = itemView.findViewById(R.id.tvTenCongTrinh);
            tvBienSo = itemView.findViewById(R.id.tvBienSo);
            tvKhoiLuong = itemView.findViewById(R.id.tvKhoiLuong);
            tvThoiGianGiao = itemView.findViewById(R.id.tvThoiGianGiao);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
        }
    }
}
