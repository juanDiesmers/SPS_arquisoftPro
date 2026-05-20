using Microsoft.EntityFrameworkCore;
using SaludPay.Api.Models;

namespace SaludPay.Api.Data
{
    public class SaludPayDbContext : DbContext
    {
        public SaludPayDbContext(DbContextOptions<SaludPayDbContext> options) : base(options)
        {
        }

        public DbSet<PendingPayment> PendingPayments { get; set; } = null!;

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.Entity<PendingPayment>(entity =>
            {
                entity.ToTable("pending_payments");
                entity.Property(e => e.Estado).HasMaxLength(50).IsRequired();
                entity.Property(e => e.CreatedAt).HasColumnType("datetime(6)").IsRequired();
                entity.Property(e => e.UpdatedAt).HasColumnType("datetime(6)").IsRequired();
            });

            base.OnModelCreating(modelBuilder);
        }
    }
}
