package com.example.trafficlights.RecyclerView

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.trafficlights.HINT
import com.example.trafficlights.PROBLEM_ID
import com.example.trafficlights.R
import com.example.trafficlights.REQUEST_CODE_ACTIVITY_PHOTO
import com.example.trafficlights.`object`.TicketType
import com.example.trafficlights.activities.BasicTicketActivity
import com.example.trafficlights.activities.QrCodeActivity

class TicketTypeAdapter(private val context: Context, private val values: List<TicketType>) :
    RecyclerView.Adapter<TicketTypeAdapter.MyViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item, parent, false)
        return MyViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //holder.icon?.setImageURI(Uri.parse("http://84.22.135.132:5000"+values[position].url))
        Glide
            .with(holder.itemView)
            .load("http://84.22.135.132:5000"+values[position].url)
            .thumbnail(0.5f)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(holder.icon!!)
        holder.description?.text = values[holder.adapterPosition].description

        if (!values[holder.adapterPosition].QRable) {
            holder.qr?.visibility = View.INVISIBLE
        }

        holder.hint = values[holder.adapterPosition].hint.toString()

        holder.problemId = values[holder.adapterPosition].id
        holder.ticket?.setOnClickListener {
            val intent  = Intent(context, BasicTicketActivity::class.java).apply {
                putExtra(PROBLEM_ID, holder.problemId)
                putExtra(HINT, holder.hint)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            (context as Activity).startActivityForResult(intent, REQUEST_CODE_ACTIVITY_PHOTO)
        }

        holder.qr?.setOnClickListener {
            val intent  = Intent(context, QrCodeActivity::class.java).apply {
                putExtra(PROBLEM_ID, holder.problemId)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            (context as Activity).startActivityForResult(intent, REQUEST_CODE_ACTIVITY_PHOTO)
        }
    }

    override fun getItemCount(): Int {
        return values.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var icon: ImageView? = null
        var description: TextView? = null
        var qr : Button? = null
        var ticket: Button? = null
        var hint: String? = null
        var problemId :Int? = null



        init {
            icon = itemView.findViewById(R.id.iconImage)
            description = itemView.findViewById(R.id.descriptionText)
            qr = itemView.findViewById(R.id.ButtonQr)
            ticket = itemView.findViewById(R.id.ButtonTicket)
        }
    }

}